package thrift.meta

import star.meta._
import com.twitter.scrooge.frontend.ParseException
import com.twitter.scrooge.ast.Document

sealed trait Parsed[+T]
object Parsed {
  final case class Success(input: Input, tree: Document) extends Parsed[Document] {
    override def toString = tree.toString
  }
  final case class Error(pos: Position, message: String, details: ParseException) extends Parsed[Nothing] {
    override def toString = details.toString
  }
}

sealed trait Attributed[+T] extends Parsed[T]
object Attributed {
  final case class Success(input: Input, tree: Document, attrs: Attributes) extends Attributed[Document] {
    override def toString = attrs.toString
  }
  final case class Error(pos: Position, message: String, details: Exception) extends Attributed[Nothing] {
    override def toString = details.toString
  }
}
