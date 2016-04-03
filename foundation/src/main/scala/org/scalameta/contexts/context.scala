package org.scalameta.contexts

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.macros.whitebox.Context
import macrocompat.bundle

class context(translateExceptions: Boolean = false) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ContextMacros.impl
}

// // TODO: macro-compat bug?
// object ContextMacros {
//   def impl(c: scala.reflect.macros.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
//     val bundle = new ContextMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
//     c.Expr[Any](bundle.impl(annottees.map(_.tree.asInstanceOf[bundle.c.Tree]): _*).asInstanceOf[c.Tree])
//   }
// }

@bundle
class ContextMacros(val c: Context) {
  import c.universe._
  import Flag._
  def impl(annottees: Tree*): Tree = {
    val args = c.macroApplication match {
      case q"new $x1(..$args).macroTransform(..$x2)" => args
      case q"new $x1().macroTransform(..$x2)" => Nil
    }
    val translateExceptions = args.collect{ case q"translateExceptions = true" => true }.nonEmpty
    def transform(cdef: ClassDef): ClassDef = {
      val q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" = cdef
      val stats1 = stats.map {
        case stat @ DefDef(mods, name, tparams, vparamss, tpt, body) if translateExceptions =>
          val rethrow = {
            val isSemantic = !c.internal.enclosingOwner.fullName.contains(".artifacts")
            if (isSemantic) q"throw new _root_.scala.meta.semantic.SemanticException(other.getMessage, other)"
            else q"throw new _root_.scala.meta.artifacts.ArtifactException(artifact, other.getMessage, other)"
          }
          val body1 = q"""
            try $body
            catch {
              case ex: _root_.scala.meta.ScalametaException => throw ex
              case ex: _root_.scala.meta.ScalametaError => throw ex
              case other: _root_.scala.Exception => $rethrow
            }
          """
          treeCopy.DefDef(stat, mods, name, tparams, vparamss, tpt, body1)
        case other =>
          other
      }
      q"$mods class $name[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats1 }"
    }
    val expanded = annottees match {
      case (cdef: ClassDef) :: rest if !cdef.mods.hasFlag(TRAIT) => transform(cdef) +: rest
      case annottee :: rest => c.abort(annottee.pos, "only classes can be @context")
    }
    q"{ ..$expanded; () }"
  }
}
