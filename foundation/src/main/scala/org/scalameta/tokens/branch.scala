package org.scalameta.tokens

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import macrocompat.bundle

class branch extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro BranchMacros.impl
}

// // TODO: macro-compat bug?
// object BranchMacros {
//   def impl(c: scala.reflect.macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
//     val bundle = new BranchMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
//     c.Expr[Any](bundle.impl(annottees.map(_.tree.asInstanceOf[bundle.c.Tree]): _*).asInstanceOf[c.Tree])
//   }
// }

@bundle
class BranchMacros(val c: Context) {
  import c.universe._
  import Flag._
  def impl(annottees: Tree*): Tree = {
    def transform(cdef: ClassDef): ClassDef = {
      val ClassDef(mods @ Modifiers(flags, privateWithin, anns), name, tparams, templ) = cdef
      val Adt = q"_root_.org.scalameta.adt"
      val TokenInternal = q"_root_.org.scalameta.tokens.internal"
      val anns1 = q"new $TokenInternal.branch" +: q"new $Adt.branch" +: anns
      ClassDef(Modifiers(flags, privateWithin, anns1), name, tparams, templ)
    }
    val expanded = annottees match {
      case (cdef @ ClassDef(mods, _, _, _)) :: rest if mods.hasFlag(TRAIT) => transform(cdef) :: rest
      case annottee :: rest => c.abort(annottee.pos, "only traits can be @branch")
    }
    q"{ ..$expanded; () }"
  }
}