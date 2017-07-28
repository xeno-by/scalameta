package thrift

import com.twitter.scrooge.ast._
import com.twitter.scrooge.frontend._
import star.meta._

package object meta {
  implicit class XtensionInputLike[T](inputLike: T) {
    def parse(implicit convert: Convert[T, Input]): Parsed[Document] = {
      Parse.apply(convert.apply(inputLike), Importer(Nil))
    }
    def attribute(implicit convert: Convert[T, Input]): Attributed[Document] = {
      inputLike.parse match {
        case Parsed.Error(pos, message, details) =>
          Attributed.Error(pos, message, details)
        case Parsed.Success(input, tree) =>
          Attribute.apply(input, tree)
        case _ =>
          ???
      }
    }
  }
}
