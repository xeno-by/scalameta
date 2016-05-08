package scala.meta
package internal

import org.scalameta._
import org.scalameta.invariants._
import scala.meta.classifiers._
import scala.meta.inputs._
import scala.meta.tokens._
import scala.meta.tokens.Token._

package object tokens {
  implicit class XtensionTokenName(token: Token) {
    def name: String = {
      import scala.reflect.runtime.{universe => ru}
      object TokenReflection extends {
        val u: ru.type = ru
        val mirror: u.Mirror = u.runtimeMirror(classOf[Token].getClassLoader)
      } with scala.meta.internal.tokens.Reflection
      import TokenReflection._
      val mirror = ru.runtimeMirror(classOf[Token].getClassLoader)
      mirror.classSymbol(token.getClass).asLeaf.tokenName
    }
  }

  implicit class XtensionTokensTranslatePosition(tokens: Tokens) {
    // NOTE: If this method changes, go and change the "freeform tokens" test.
    // TODO: I don't like the number of special cases and validations inside this method.
    def translatePosition(pos: Position): TokenStreamPosition = {
      def fail(message: String) = throw new IllegalArgumentException("internal error: " + message)
      def failPositionEmpty() = fail("can't slice according to an empty position")
      def failMissingLetterbox() = fail("can't slice without the BOF .. EOF letterbox")
      def failEmptySyntax() = fail("can't slice empty syntax")
      def failDoesntLineUp() = fail(s"tokens in $tokens don't line up according to $pos")
      def failWrongInput(badInput: Input) = fail(s"tokens in $tokens have wrong input according to $pos: expected = ${pos.input}, actual = $badInput")
      pos match {
        case Position.Range(input, start, end) =>
          def validateTokens(): Unit = {
            if (tokens.length < 2 || !tokens.head.is[BOF] || !tokens.last.is[EOF]) failMissingLetterbox()
            if (tokens.forall(token => token.start == token.end)) failEmptySyntax()
          }
          def find(offset: Int, start: Boolean): Int = {
            def coord(idx: Int) = {
              if (start) tokens(idx).start else tokens(idx).end
            }
            def binarySearch(): Int = {
              // Bounds are inclusive
              var lo = 0
              var hi = tokens.length - 1
              while (lo <= hi) {
                val mid = (lo + hi) / 2
                if (offset < coord(mid)) hi = mid - 1
                else if (offset == coord(mid)) return mid
                else /* if (coord(mid) < offset) */ lo = mid + 1
              }
              return -1
            }
            def disambiguate(idx0: Int): Int = {
              def badToken(idx: Int) = {
                // These are tokens that are empty (i.e. of zero length)
                // and that can't be first/last tokens of an abstract syntax tree.
                if (idx < 0 || idx >= tokens.length) failDoesntLineUp();
                tokens(idx).is[BOF] || tokens(idx).is[EOF] || // NOTE: if BOF/EOF here changes, go and change ScalametaParser.parseRule
                tokens(idx).is[Interpolation.SpliceEnd] || tokens(idx).is[Xml.SpliceStart] || tokens(idx).is[Xml.SpliceEnd]
              }
              var idx = idx0
              if (badToken(idx)) {
                val step = if (start) +1 else -1
                while (badToken(idx)) idx += step
              } else {
                val step = if (start) -1 else +1
                while (!badToken(idx) && coord(idx) == offset) idx += step
                idx -= step
              }
              require(!badToken(idx) && debug(tokens, pos, idx0, idx))
              idx
            }
            // Find a token that starts/ends at a given offset
            // and then disambiguate with other tokens that look the same.
            val idx = binarySearch()
            if (idx == -1) failDoesntLineUp()
            disambiguate(idx)
          }
          def validateResult(lo: Int, hi: Int): Unit = {
            require(start.offset == tokens(lo).start && debug(pos, lo, hi))
            require(end.offset == tokens(hi).end && debug(pos, lo, hi))
            var i = lo
            while (i <= hi) {
              if (pos.input != tokens(i).input) failWrongInput(tokens(i).input)
              i += 1
            }
          }
          validateTokens()
          val lo = find(start.offset, start = true)
          val hi = find(end.offset, start = false)
          validateResult(lo, hi)
          TokenStreamPosition(pos.input, lo, hi + 1)
        case _ =>
          failPositionEmpty()
      }
    }
  }
}
