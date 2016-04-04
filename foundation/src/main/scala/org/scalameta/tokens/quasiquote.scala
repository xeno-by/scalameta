package org.scalameta.tokens

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import macrocompat.bundle

class quasiquote[T](qname: scala.Symbol) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro QuasiquoteMacros.impl
}

// TODO: macro-compat bug?
object QuasiquoteMacros {
  def impl(c: scala.reflect.macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val bundle = new QuasiquoteMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
    c.Expr[Any](bundle.impl(annottees.map(_.tree.asInstanceOf[bundle.c.Tree]): _*).asInstanceOf[c.Tree])
  }
}

@bundle
class QuasiquoteMacros(val c: Context) {
  import c.universe._
  import Flag._
  val Any = tq"_root_.scala.Any"
  val ReificationMacros = q"_root_.scala.meta.internal.tokenquasiquotes.ReificationMacros"
  val Dialect = tq"_root_.scala.meta.Dialect"
  val Tokens = tq"_root_.scala.meta.tokens.Tokens"
  def impl(annottees: c.Tree*): c.Tree = {
    val q"new $x1[..$qtypes](scala.Symbol(${qname: String})).macroTransform(..$x2)" = c.macroApplication
    def transform(cdef: ClassDef, mdef: ModuleDef): List[ImplDef] = {
      val q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = cdef
      val stats1 = stats :+ q"""
        object ${TermName(qname)} {
          import _root_.scala.language.experimental.macros
          def apply(args: $Any*)(implicit dialect: $Dialect): $Tokens = macro $ReificationMacros.apply
          def unapply(scrutinee: $Any)(implicit dialect: $Dialect): $Any = macro $ReificationMacros.unapply
        }
      """
      val cdef1 = q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats1 }"
      List(cdef1, mdef)
    }
    val expanded = annottees match {
      case (cdef: ClassDef) :: (mdef: ModuleDef) :: rest if cdef.mods.hasFlag(IMPLICIT) => transform(cdef, mdef) ++ rest
      case (cdef: ClassDef) :: rest if cdef.mods.hasFlag(IMPLICIT) => transform(cdef, q"object ${cdef.name.toTermName}") ++ rest
      case annottee :: rest => c.abort(annottee.pos, "only implicit classes can be @quasiquote")
    }
    q"{ ..$expanded; () }"
  }
}

