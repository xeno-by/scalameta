package org.scalameta

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context

package object roles {
  implicit class XtensionRole[T](x: T) {
    def is[R, E](e: E)(implicit ev: CanEnroll[T, R, E]): Boolean = {
      e.get(x).isDefined
    }

    def with[R, E](c: R)(implicit ev: CanEnroll[T, R, E]): T = {
      ev.e.set(x, c)
    }

    def as[R, E](e: E)(implicit ev: CanEnroll[T, R, E]): R = {
      e.get(x).get
    }
  }
}

package roles {
  trait Role[T]

  trait Enroller[T, R <: Role[T]] {
    def get(x: T): Option[R]
    def set(x: T, c: R): T
  }

  trait CanEnroll[T, R <: Role[T], E <: Enroller[T, R]] {
    def e: Enroller[T, R]
  }

  class role extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro RoleMacros.role
  }

  class RoleMacros(val c: Context) {
    import c.universe._
    import definitions._

    // 1) @role object ... extends Role[$T]
    // * extend Enroller
    // * generate get/set based on test/mark
    // * create an instance of CanEnroll

    // 2) @role object ... extends ReadonlyRole[$T]
    // * extend Enroller
    // * generate get based on test
    // * generate set that just requires get to be true
    // * create an instance of CanEnroll

    // 3) @role class ... extends Role[$T]
    // * create a companion that extends Enroller
    // * create an instance of Enroll

    // 4) @role class ... extends ReadonlyRole[$T]
    // * create a companion that extends Enroller
    // * generate set that just requires get to be true
    // * create an instance of Enroll

    def role(annottees: Tree*): Tree = {
      ???
    }
  }
}
