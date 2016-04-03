package org.scalameta.tokens

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import org.scalameta.unreachable
import org.scalameta.adt.{LiftableMacros => AdtLiftableMacros}
import org.scalameta.tokens.internal.Token
import macrocompat.bundle

trait Liftables {
  val u: scala.reflect.macros.Universe
  implicit def materializeToken[T <: Token]: u.Liftable[T] = macro LiftableMacros.impl[T]
}

// TODO: macro-compat bug?
object LiftableMacros {
  def impl[T <: Adt](c: scala.reflect.macros.Context)(implicit T: c.WeakTypeTag[T]) = {
    val bundle = new LiftableMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
    c.Expr(bundle.impl[T](T.asInstanceOf[bundle.c.WeakTypeTag[T]]).asInstanceOf[c.Tree])
  }
}

@bundle
class LiftableMacros(override val c: Context) extends AdtLiftableMacros(c) {
  import c.universe._
  lazy val UnquoteClass = c.mirror.staticModule("scala.meta.tokens.Token").asModule.info.member(TypeName("Unquote")).asClass
  override def customMatcher(adt: Adt, defName: TermName, localName: TermName): Option[DefDef] = {
    // TODO: see comments to ast.LiftableMacros
    def redirectTo(methodName: String) = q"def $defName($localName: ${adt.tpe}): ${c.prefix}.u.Tree = ${TermName(methodName)}.apply($localName)"
    if (adt.tpe <:< UnquoteClass.toType) Some(redirectTo("liftUnquote"))
    else None
  }
}