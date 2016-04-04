package org

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.compat.Platform.EOL
import macrocompat.bundle

package object scalameta {
  def unreachable: Nothing = macro UnreachableMacros.unreachable
  def unreachable(debug: Boolean): Nothing = macro UnreachableMacros.unreachableDebug
}

package scalameta {
  class UnreachableError(message: String) extends Error(message)
  object UnreachableError {
    def raise(debuggees: Map[String, Any]): Nothing = {
      val mandatory = "this code path should've been unreachable"
      val optional = if (debuggees.nonEmpty) EOL + debuggees.toList.sortBy(_._1).map({ case (k, v) => s"where $k = $v"}).mkString(EOL) else ""
      throw new UnreachableError(mandatory + optional)
    }
  }

  // TODO: macro-compat bug?
  object UnreachableMacros {
    def unreachable(c: scala.reflect.macros.Context): c.Expr[Nothing] = {
      val bundle = new UnreachableMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
      c.Expr[Nothing](bundle.unreachable.asInstanceOf[c.Tree])
    }
    def unreachableDebug(c: scala.reflect.macros.Context)(debug: c.Expr[Boolean]): c.Expr[Nothing] = {
      val bundle = new UnreachableMacros(new macrocompat.RuntimeCompatContext(c.asInstanceOf[scala.reflect.macros.runtime.Context]))
      c.Expr[Nothing](bundle.unreachableDebug(debug.tree.asInstanceOf[bundle.c.Tree]).asInstanceOf[c.Tree])
    }
  }

  @bundle
  class UnreachableMacros(val c: Context) {
    import c.universe._
    def unreachable: c.Tree = {
      val debuggees = Map.empty[String, Tree]
      q"_root_.org.scalameta.UnreachableError.raise($debuggees)"
    }
    def unreachableDebug(debug: c.Tree): c.Tree = {
      object debugFinder extends Traverser {
        private val invariantsPackageObject = c.mirror.staticPackage("org.scalameta.invariants").info.member(termNames.PACKAGE).asModule
        private val invariantsDebug = invariantsPackageObject.info.member(TermName("debug")).asMethod
        val debuggees = scala.collection.mutable.ListBuffer[Tree]()
        override def traverse(tree: Tree): Unit = tree match {
          case Apply(fun, args) if fun.symbol == invariantsDebug => debuggees ++= args
          case tree => super.traverse(tree)
        }
      }
      debugFinder.traverse(debug)
      val debuggees = debugFinder.debuggees.map(tree => tree.toString -> tree.duplicate).toMap
      q"_root_.org.scalameta.UnreachableError.raise($debuggees)"
    }
  }
}