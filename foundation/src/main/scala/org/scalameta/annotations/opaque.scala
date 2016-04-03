package org.scalameta.annotations

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import macrocompat.bundle

class opaque(exclude: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro OpaqueMacros.impl
}

// TODO: macro-compat bug?
object OpaqueMacros {
  def impl(c: scala.reflect.macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val bundle = new OpaqueMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
    c.Expr[Any](bundle.impl(annottees.map(_.tree.asInstanceOf[bundle.c.Tree]): _*).asInstanceOf[c.Tree])
  }
}

@bundle
class OpaqueMacros(val c: Context) {
  import c.universe._
  import Flag._
  def impl(annottees: c.Tree*): c.Tree = {
    val args = c.macroApplication match {
      case q"new $x1(..$args).macroTransform(..$x2)" => args
      case q"new $x1().macroTransform(..$x2)" => Nil
    }
    val exclude = "^" + args.collect{ case q"exclude = ${s: String}" => s }.headOption.getOrElse("") + "$"
    def transform(impl: Template): Template = {
      val Template(parents, self, body) = impl
      val body1 = body.map({
        case q"${mods @ Modifiers(flags, privateWithin, anns)} def $name[..$tparams](...$paramss): $tpt = $body"
        if !java.util.regex.Pattern.matches(exclude, name.toString) =>
          val shouldPrivatize = !mods.hasFlag(PRIVATE) && !mods.hasFlag(PROTECTED) && privateWithin == typeNames.EMPTY
          val privateWithin1 = if (shouldPrivatize) TypeName("meta") else privateWithin
          q"${Modifiers(flags, privateWithin1, anns)} def $name[..$tparams](...$paramss): $tpt = $body"
        case stat =>
          stat
      })
      Template(parents, self, body1)
    }
    val expanded = annottees match {
      case (cdef @ ClassDef(mods, name, tparams, impl)) :: rest => treeCopy.ClassDef(cdef, mods, name, tparams, transform(impl)) :: rest
      case (mdef @ ModuleDef(mods, name, impl)) :: rest => treeCopy.ModuleDef(mdef, mods, name, transform(impl)) :: rest
      case annottee :: rest => c.abort(annottee.pos, "only classes, traits and objects can be @hosted")
    }
    q"{ ..$expanded; () }"
  }
}
