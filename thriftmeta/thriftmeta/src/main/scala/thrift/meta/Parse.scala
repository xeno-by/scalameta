package thrift.meta

import com.twitter.scrooge.ast._
import com.twitter.scrooge.frontend._
import scala.util.Try
import star.meta._

object Parse {
  def apply(input: Input, importer: Importer): Parsed[Document] = {
    val importer = Importer(Seq())
    val parser = new ThriftParser(importer, strict = true)
    try Parsed.Success(input, parser.parse(input.text, parser.document))
    catch { case ex: ParseException => Parsed.Error(Position.None, ex.getMessage, ex) }
  }
}