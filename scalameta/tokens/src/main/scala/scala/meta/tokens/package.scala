package scala.meta

package object tokens extends tokens.Api {
  // NOTE: Typically we don't add aliases into package objects of dedicated modules.
  // This is done because these aliases typically point to classes/objects declared in the package,
  // so this won't be of any use (or worse, will lead to ambiguous imports).
  // However, in this case, Tokens isn't defined anywhere as a standalone class, so we can do this.
  type Tokens = scala.collection.immutable.Seq[Token]
  lazy val Tokens = scala.collection.immutable.Seq
}
