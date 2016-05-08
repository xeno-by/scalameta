package scala.meta

import scala.{Seq => _}
import scala.collection.immutable.Seq
import scala.meta.prettyprinters._
import Show.{ sequence => _, repeat => r, indent => i, newline => n, _ }

package object tests {
  implicit def structureSeq[T <: Tree]: Structure[Seq[T]] = Structure { xs =>
    Sequence(Str("Seq("), r(xs.map(x => implicitly[Structure[T]].apply(x)), ", "), Str(")"))
  }

  implicit def structureSeqSeq[T <: Tree]: Structure[Seq[Seq[T]]] = Structure { xs =>
    Sequence(Str("Seq("), r(xs.map(x => implicitly[Structure[Seq[T]]].apply(x)), ", "), Str(")"))
  }

  implicit def structureOption[T <: Tree]: Structure[Option[T]] = Structure {
    case scala.Some(x) => Sequence(Str("Some("), implicitly[Structure[T]].apply(x), Str(")"))
    case scala.None => Str("None")
  }
}