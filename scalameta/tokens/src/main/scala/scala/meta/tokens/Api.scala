package scala.meta
package tokens

import scala.meta.common._
import scala.meta.inputs._
import scala.meta.prettyprinters._
import scala.meta.prettyprinters.Syntax.Options
import scala.meta.internal.prettyprinters._

private[meta] trait Api {
}

private[meta] trait Aliases {
  type Token = scala.meta.tokens.Token
  lazy val Token = scala.meta.tokens.Token

  type Tokens = scala.collection.immutable.Seq[Token]
  lazy val Tokens = scala.collection.immutable.Seq
}