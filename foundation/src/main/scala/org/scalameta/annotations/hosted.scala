package org.scalameta.annotations

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import macrocompat.bundle

class hosted extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro HostedMacros.impl
}

// TODO: macro-compat bug?
object HostedMacros {
  def impl(c: scala.reflect.macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val bundle = new HostedMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
    c.Expr[Any](bundle.impl(annottees.map(_.tree.asInstanceOf[bundle.c.Tree]): _*).asInstanceOf[c.Tree])
  }
}

@bundle
class HostedMacros(val c: Context) {
  import c.universe._
  def impl(annottees: c.Tree*): c.Tree = {
    val args = c.macroApplication match {
      case q"new $x1(..$args).macroTransform(..$x2)" => args
      case q"new $x1().macroTransform(..$x2)" => Nil
    }
    val contextTpt = {
      def enclosingPackage(sym: Symbol): Symbol = {
        if (sym == NoSymbol) sym
        else if (sym.isPackage || sym.isPackageClass) sym
        else enclosingPackage(sym.owner)
      }
      val apiSym = enclosingPackage(c.internal.enclosingOwner)
      val apiRef = apiSym.fullName.split('.').foldLeft(q"_root_": Tree)((acc, part) => q"$acc.${TermName(part)}")
      tq"$apiRef.Context"
    }
    def transform(ddef: DefDef): DefDef = {
      val DefDef(mods, name, tparams, vparamss, tpt, body) = ddef
      val contextful = q"new _root_.org.scalameta.annotations.contextful[$contextTpt]"
      val mods1 = Modifiers(mods.flags, mods.privateWithin, mods.annotations ++ List(contextful))
      DefDef(mods1, name, tparams, vparamss, tpt, body)
    }
    val expanded = annottees match {
      case (ddef: DefDef) :: rest => transform(ddef) :: rest
      case annottee :: rest => c.abort(annottee.pos, "only methods can be @hosted")
    }
    q"{ ..$expanded; () }"
  }
}
