package org.langmeta.sql

import java.io._
import java.nio.file._
import java.sql._
import scala.Array
import scala.collection.mutable
import scala.io.Codec
import scala.reflect.classTag
import scala.util.control.NonFatal
import ammonite.ops.%%
import ammonite.ops.ImplicitWd._
import org.{langmeta => hi}
import org.langmeta.internal.semanticdb.{schema => lo}

object Index {
  def main(args: Array[String]): Unit = {
    val connRegex = "jdbc:(.*?):.*".r
    args match {
      case Array(connString @ connRegex(database), semanticdbGlobs @ _*) =>
        val semanticdbFilenames = semanticdbGlobs.flatMap { semanticdbGlob =>
          val expandGlob = s"""
            |import glob, os
            |for filename in glob.glob(os.path.expanduser("$semanticdbGlob")):
            |  print filename
          """.trim.stripMargin
          %%("python", "-c", expandGlob).out.lines
        }

        val appStart = System.nanoTime()
        var genuineDocuments = 0
        class Counter { var value = 0; def next() = { value += 1; value }; }
        Class.forName("org.sqlite.JDBC")
        Class.forName("com.mysql.cj.jdbc.Driver")
        val conn = DriverManager.getConnection(connString)
        conn.setAutoCommit(false)
        try {
          val preImportSql = loadSql("semanticdbPreImport", database)
          val statement = conn.createStatement()
          statement.executeUpdate(preImportSql.mkString)

          def tableInsertStmt(table: String): PreparedStatement = {
            val rs = statement.executeQuery("select * from " + table)
            val rsmd = rs.getMetaData()
            val columnCount = rsmd.getColumnCount()
            val columnNames = 1.to(columnCount).map(rsmd.getColumnName)
            val s_columns = "(" + columnNames.mkString(", ") + ")"
            val columnValues = "?" * columnNames.length
            val s_values = "(" + columnValues.mkString(", ") + ")"
            val sql = s"insert into $table $s_columns values $s_values"
            conn.prepareStatement(sql)
          }
          val documentStmt = tableInsertStmt("document")
          val nameStmt = tableInsertStmt("name")
          val messageStmt = tableInsertStmt("message")
          val symbolStmt = tableInsertStmt("symbol")
          val syntheticStmt = tableInsertStmt("synthetic")

          def persistBatch() = {
            println("Executing document batch...")
            documentStmt.executeBatch()
            conn.commit()
            println("Executing name batch...")
            nameStmt.executeBatch()
            conn.commit()
            println("Executing message batch...")
            messageStmt.executeBatch()
            conn.commit()
            println("Executing symbol batch...")
            symbolStmt.executeBatch()
            conn.commit()
            println("Executing synthetic batch...")
            syntheticStmt.executeBatch()
            conn.commit()
          }

          val semanticdbStart = System.nanoTime()
          val documentsToPaths = mutable.Map[String, Path]()
          var documentId = new Counter()
          var nameId = new Counter()
          var messageId = new Counter()
          var _symbolId = new Counter()
          var symbolIds = mutable.Map[String, Int]()
          var symbolTodo = mutable.Map[Int, String]()
          var symbolDone = mutable.Set[Int]()
          def symbolId(symbol: String): Int = {
            if (symbolIds.contains(symbol)) {
              symbolIds(symbol)
            } else {
              val symbolId = _symbolId.next()
              symbolIds(symbol) = symbolId
              symbolTodo(symbolId) = symbol
              symbolId
            }
          }
          val syntheticId = new Counter()
          def reportProgress(): Unit = {
            val buf = new StringBuilder
            buf.append(s"$genuineDocuments documents: ")
            buf.append(s"${nameId.value} names, ")
            buf.append(s"${messageId.value} messages, ")
            buf.append(s"${_symbolId.value} symbols, ")
            buf.append(s"${syntheticId.value} synthetics")
            println(buf.toString)
          }

          semanticdbFilenames.foreach { semanticdbFilename =>
            val path = Paths.get(semanticdbFilename)
            try {
              val bytes = Files.readAllBytes(path)
              val db = lo.Database.parseFrom(bytes)
              db.documents.foreach { document =>
                documentsToPaths.get(document.filename) match {
                  case Some(existingPath) =>
                    val what = document.filename
                    val details = s"both $existingPath and $path"
                    println(s"Duplicate document filename $what in $details")
                  case None =>
                    genuineDocuments += 1
                    documentsToPaths(document.filename) = path

                    val input = hi.Input.VirtualFile(document.filename, document.contents)
                    implicit class PositionOps(position: Option[lo.Position]) {
                      def toHi: hi.Position = {
                        val start = position.get.start
                        val end = position.get.end
                        hi.Position.Range(input, start, end)
                      }
                    }

                    val documentRef = documentId.next
                    documentStmt.setInt(1, documentRef)
                    documentStmt.setString(2, document.filename)
                    documentStmt.setString(3, "")
                    documentStmt.setString(4, document.language)
                    documentStmt.addBatch()

                    document.names.foreach { name =>
                      nameStmt.setInt(1, nameId.next)
                      nameStmt.setInt(2, documentRef)
                      nameStmt.setInt(3, name.position.toHi.startLine)
                      nameStmt.setInt(4, name.position.toHi.startColumn)
                      nameStmt.setInt(5, name.position.toHi.endLine)
                      nameStmt.setInt(6, name.position.toHi.endColumn)
                      nameStmt.setInt(7, symbolId(name.symbol))
                      nameStmt.setBoolean(8, name.isDefinition)
                      nameStmt.addBatch()
                    }

                    document.messages.foreach { message =>
                      messageStmt.setInt(1, messageId.next)
                      messageStmt.setInt(2, documentRef)
                      messageStmt.setInt(3, message.position.toHi.startLine)
                      messageStmt.setInt(4, message.position.toHi.startColumn)
                      messageStmt.setInt(5, message.position.toHi.endLine)
                      messageStmt.setInt(6, message.position.toHi.endColumn)
                      messageStmt.setInt(7, message.severity.value)
                      messageStmt.setString(8, message.text)
                      messageStmt.addBatch()
                    }

                    document.symbols.foreach { symbol =>
                      val symbolRef = symbolId(symbol.symbol)
                      if (!symbolDone.contains(symbolRef)) {
                        symbolStmt.setInt(1, symbolRef)
                        symbolStmt.setString(2, symbol.symbol)
                        symbolStmt.setLong(3, symbol.denotation.get.flags)
                        symbolStmt.setString(4, symbol.denotation.get.name)
                        val signatureDocumentRef = {
                          documentStmt.setInt(1, documentId.next)
                          documentStmt.setString(2, null)
                          documentStmt.setString(3, symbol.denotation.get.signature)
                          documentStmt.setString(4, null)
                          documentStmt.addBatch()
                          documentId.value
                        }
                        symbolStmt.setInt(5, signatureDocumentRef)
                        symbol.denotation.get.names.foreach { name =>
                          nameStmt.setInt(1, nameId.next)
                          nameStmt.setInt(2, signatureDocumentRef)
                          nameStmt.setInt(3, 0)
                          nameStmt.setInt(4, name.position.get.start)
                          nameStmt.setInt(5, 0)
                          nameStmt.setInt(6, name.position.get.end)
                          nameStmt.setInt(7, symbolId(name.symbol))
                          nameStmt.setBoolean(8, name.isDefinition)
                          nameStmt.addBatch()
                        }
                        symbolStmt.addBatch()
                        symbolTodo.remove(symbolRef)
                        symbolDone.add(symbolRef)
                      }
                    }

                    document.synthetics.foreach { synthetic =>
                      syntheticStmt.setInt(1, syntheticId.next)
                      syntheticStmt.setInt(2, documentRef)
                      syntheticStmt.setInt(3, synthetic.pos.toHi.startLine)
                      syntheticStmt.setInt(4, synthetic.pos.toHi.startColumn)
                      syntheticStmt.setInt(5, synthetic.pos.toHi.endLine)
                      syntheticStmt.setInt(6, synthetic.pos.toHi.endColumn)
                      val syntheticDocumentRef = {
                        documentStmt.setInt(1, documentId.next)
                        documentStmt.setString(2, null)
                        documentStmt.setString(3, synthetic.text)
                        documentStmt.setString(4, null)
                        documentStmt.addBatch()
                        documentId.value
                      }
                      syntheticStmt.setInt(7, syntheticDocumentRef)
                      synthetic.names.foreach { name =>
                        nameStmt.setInt(1, nameId.next)
                        nameStmt.setInt(2, syntheticDocumentRef)
                        nameStmt.setInt(3, 0)
                        nameStmt.setInt(4, name.position.get.start)
                        nameStmt.setInt(5, 0)
                        nameStmt.setInt(6, name.position.get.start)
                        nameStmt.setInt(7, symbolId(name.symbol))
                        nameStmt.setBoolean(8, name.isDefinition)
                        nameStmt.addBatch()
                      }
                      syntheticStmt.addBatch()
                    }

                    if ((genuineDocuments % 10) == 0) reportProgress()
                }
              }
            } catch {
              case NonFatal(ex) =>
                println(s"Error processing $path")
                ex.printStackTrace
            }

            persistBatch()
          }

          // NOTE: Remaining symbols that haven't yet been mentioned
          // in document.symbols must be flushed using default metadata.
          symbolTodo.foreach {
            case (symbolId, symbol) =>
              symbolStmt.setInt(1, symbolId)
              symbolStmt.setString(2, symbol)
              symbolStmt.setInt(3, 0)
              symbolStmt.setString(4, null)
              symbolStmt.setInt(5, 0)
              symbolStmt.addBatch()
          }

          persistBatch()

          if ((genuineDocuments % 10) != 0) reportProgress()

          val postImportSql = loadSql("semanticdbPostImport", database)
          statement.executeUpdate(preImportSql.mkString)
        } finally {
          //commit more often
          conn.commit()
          conn.close()
          val appCPU = (System.nanoTime() - appStart) * 1.0 / 1000000000
          val appPerformance = genuineDocuments / appCPU
          println(s"CPU time: ${"%.3f".format(appCPU)}s")
          println(s"Performance: ${"%.3f".format(appPerformance)} documents/s")
          if (database == "sqlite") {
            val sqliteFilename = connString.stripPrefix("jdbc:sqlite:")
            val appDisk = new File(sqliteFilename).length * 1.0 / 1024 / 1024
            println(s"Disk size: ${"%.1f".format(appDisk)} MB")
          }
        }
      case _ =>
        val objectName = classTag[Index.type].toString.stripSuffix("$")
        println(s"usage: $objectName <connection string> [<glob> <glob> ...]")
        println(s"If connecting to MySQL ensure rewriteBatchedStatements=true is set in connection string for best performance")
        sys.exit(1)
    }
  }

  def loadSql(sqlName: String, databaseKind: String): String = {
    val fileName = s"$sqlName.$databaseKind.sql"
    val fileUrl = getClass.getClassLoader.getResource(fileName)
    val fileStream = fileUrl.openStream
    if (fileStream == null) sys.error(s"failed to load $fileName")
    try {
      scala.io.Source.fromInputStream(fileStream)(Codec.UTF8).mkString
    }
    finally {
      fileStream.close()
    }
  }
}
