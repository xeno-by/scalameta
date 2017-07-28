package scala.meta.internal
package scalahost

import java.io._
import java.net.URI
import java.nio.charset._
import scala.compat.Platform.EOL
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins.PluginComponent
import scala.util.control.NonFatal
import scala.util.matching
import scala.{meta => m}
import scala.meta.io._
import scala.meta.internal.io.FileIO
import scala.meta.internal.semantic.DatabaseOps
import scala.meta.internal.semantic.{vfs => v}
import scala.tools.nsc.doc.ScaladocGlobal

trait ScalahostPipeline extends DatabaseOps { self: ScalahostPlugin =>
  lazy val scalametaTargetroot = AbsolutePath(
    new File(
      global.settings.outputDirs.getSingleOutput
        .map(_.file.getAbsolutePath)
        .getOrElse(global.settings.d.value)))
  implicit class XtensionURI(uri: URI) { def toFile: File = new File(uri) }

  object ScalahostComponent extends PluginComponent {
    val global: ScalahostPipeline.this.global.type = ScalahostPipeline.this.global
    val runsAfter = List("typer")
    override val runsRightAfter = Some("typer")
    val phaseName = "scalameta"
    override val description = "compute the scala.meta semantic database"
    def newPhase(_prev: Phase) = new ScalahostPhase(_prev)

    class ScalahostPhase(prev: Phase) extends StdPhase(prev) {
      override def apply(unit: g.CompilationUnit): Unit = {
        if (g.isInstanceOf[ScaladocGlobal]) return
        if (config.semanticdb.isDisabled) return

        try {
          if (config.semanticdb.isDisabled || !unit.source.file.name.endsWith(".scala")) return
          val mminidb = m.Database(List(unit.toAttributes))
          mminidb.save(scalametaTargetroot, config.sourceroot)

          val rxThriftSource = """@com.twitter.scrooge.thriftSource\("(.*?)"\)""".r
          rxThriftSource.findFirstMatchIn(unit.source.content) match {
            case Some(result) =>
              import thrift.meta._
              val thriftInput = {
                val path = RelativePath(result.group(1))
                val contents = FileIO.slurp(path.toAbsolute, Charset.forName("UTF-8"))
                m.Input.VirtualFile(path.toString, contents)
              }
              thriftInput.attribute match {
                case Attributed.Success(_, _, attrs) =>
                  val mminidb1 = m.Database(List(attrs))
                  mminidb1.save(scalametaTargetroot, config.sourceroot)
                case Attributed.Error(_, _, ex) =>
                  throw ex
              }
            case None =>
              // do nothing
          }
         } catch {
          case NonFatal(ex) =>
            val writer = new StringWriter()
            val path = unit.source.file.path
            writer.write(s"failed to generate semanticdb for $path:$EOL")
            ex.printStackTrace(new PrintWriter(writer))
            val msg = writer.toString
            import scala.meta.internal.semantic.FailureMode._
            config.failures match {
              case Error => global.reporter.error(g.NoPosition, msg)
              case Warning => global.reporter.warning(g.NoPosition, msg)
              case Info => global.reporter.info(g.NoPosition, msg, force = true)
              case Ignore => // do nothing.
            }
        }
      }

      override def run(): Unit = {
        val vdb = v.Database.load(Classpath(scalametaTargetroot))
        val orphanedVentries = vdb.entries.filter(ventry => {
          val sourceName = v.SemanticdbPaths.toSource(ventry.fragment.name)
          !config.sourceroot.resolve(sourceName).isFile
        })
        orphanedVentries.map(ve => {
          def cleanupUpwards(file: File): Unit = {
            if (file != null) {
              if (file.isFile) {
                file.delete()
              } else {
                if (file.getAbsolutePath == ve.base.toString) return
                if (file.listFiles.isEmpty) file.delete()
              }
              cleanupUpwards(file.getParentFile)
            }
          }
          cleanupUpwards(ve.uri.toFile)
        })
        super.run()
      }
    }
  }
}
