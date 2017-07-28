package thrift.meta

import com.twitter.scrooge.ast._
import scala.collection.mutable
import star.meta._

object Attribute {
  def apply(input: Input, tree: Document): Attributed[Document] = {
    val names = mutable.ListBuffer[ResolvedName]()
    val namespaces = tree.headers.collect { case Namespace(_, id) => "_root_." + id.fullName + "." }
    def bind(suffix: String, sid: SimpleID, tree: Node): Unit = {
      import scala.util.parsing.input.{OffsetPosition, NoPosition}
      val pos = {
        // TODO: Scrooge AST doesn't have positions, so we are forced to work around.
        // The workaround is pretty ridiculous, but what can we do lol.
        val start = input.text.indexOf(sid.name)
        val end = start + sid.name.length
        Position.Range(input, start, end)
      }
      val s_symbols = namespaces.map(_ + suffix)
      s_symbols.foreach(s_symbol => names += ResolvedName(pos, Symbol(s_symbol), isBinder = true))
    }
    def erase(tpe: TypeNode): String = tpe match {
      case Void => "V"
      case TBool => "Z"
      case TByte => "B"
      case TI16 => "S"
      case TI32 => "I"
      case TI64 => "L"
      case TDouble => "D"
      case TString => "Ljava/lang/String;"
      case TBinary => "[B;"
      case _ => "X" // TODO: ignore for the time being
    }
    def sig(fn: Function): String = {
      val Function(sid, _, ret, params, _, _, _) = fn
      sid.name + "(" + params.map(p => erase(p.fieldType)).mkString("") + ")" + erase(ret)
    }
    def loop(prefix: String, tree: Node): Unit = tree match {
      case Service(sid, _, fns, _, _) =>
        val suffix = prefix + sid.name + "."
        bind(suffix, sid, tree)
        fns.foreach(fn => loop(suffix, fn))
      case fn @ Function(sid, _, _, params, _, _, _) =>
        val suffix = prefix + sig(fn) + "."
        bind(suffix, sid, tree)
        params.foreach(p => loop(suffix, p))
      case Field(_, sid, _, _, _, _, _, _, _) =>
        val suffix = prefix + "(" + sid.name + ")"
        bind(suffix, sid, tree)
      case _ =>
        // TODO: ignore for the time being
    }
    tree.defs.foreach(defn => loop("", defn))
    Attributed.Success(input, tree, Attributes(input, "Thrift", names.toList, Nil, Nil, Nil))
  }
}