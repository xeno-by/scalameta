package scala.meta.internal.scalacp

import java.nio.file._
import scala.meta.internal.metacp._
import scala.meta.internal.{semanticdb => s}
import scala.meta.internal.semanticdb.Accessibility.{Tag => a}
import scala.meta.internal.semanticdb.{Language => l}
import scala.meta.internal.semanticdb.SymbolInformation.{Kind => k}
import scala.meta.internal.semanticdb.Scala._
import scala.tools.scalap.scalax.rules.scalasig._

class Scalacp private (classfile: ToplevelClassfile)
    extends AnnotationOps
    with NameOps
    with SymbolInformationOps
    with SymbolOps
    with TypeOps {
  def parse(): Option[ToplevelInfos] = {
    val bytes = Files.readAllBytes(classfile.path.toNIO)
    val scalapClassfile = ClassFileParser.parse(ByteCode(bytes))
    ScalaSigParser.parse(scalapClassfile).map { scalaSig =>
      val toplevels = scalaSig.topLevelClasses ++ scalaSig.topLevelObjects
      val others = {
        scalaSig.symbols.toList.flatMap {
          case sym: SymbolInfoSymbol if !toplevels.contains(sym) => Some(sym)
          case _ => None
        }
      }
      val stoplevels = toplevels.flatMap(sinfos)
      val sothers = toplevels.flatMap(spackages).distinct ++ others.flatMap(sinfos)
      val snonlocalOthers = sothers.filter(sinfo => !hardlinks.contains(sinfo.symbol))
      ToplevelInfos(classfile, stoplevels, snonlocalOthers)
    }
  }

  private def spackages(toplevelSym: SymbolInfoSymbol): List[s.SymbolInformation] = {
    val enclosingPackages = toplevelSym.ssym.ownerChain.init
    enclosingPackages.map { enclosingPackage =>
      s.SymbolInformation(
        symbol = enclosingPackage,
        language = l.SCALA,
        kind = k.PACKAGE,
        name = enclosingPackage.desc.name,
        accessibility = Some(s.Accessibility(a.PUBLIC)),
        owner = enclosingPackage.owner)
    }
  }

  private def sinfos(sym: SymbolInfoSymbol): List[s.SymbolInformation] = {
    if (sym.isSemanticdbLocal) return Nil
    if (sym.isUseless) return Nil
    val ssym = sym.ssym
    if (ssym.contains("$extension")) return Nil
    val sinfo = sym.toSymbolInformation(SymlinkChildren)
    if (sym.isUsefulField && sym.isMutable) {
      List(sinfo) ++ Synthetics.setterInfos(sinfo, SymlinkChildren)
    } else {
      List(sinfo)
    }
  }
}

object Scalacp {
  def parse(classfile: ToplevelClassfile): Option[ToplevelInfos] = {
    val scalacp = new Scalacp(classfile)
    scalacp.parse()
  }
}
