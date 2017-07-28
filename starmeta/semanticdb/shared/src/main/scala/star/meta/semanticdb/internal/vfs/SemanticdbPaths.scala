package star.meta.internal
package semanticdb
package vfs

import star.meta.internal.io.PathIO
import star.meta.io.RelativePath

object SemanticdbPaths {
  private val semanticdbPrefix = RelativePath("META-INF").resolve("semanticdb")
  private val semanticdbExtension = "semanticdb"

  def isSemanticdb(path: RelativePath): Boolean = {
    path.toNIO.startsWith(semanticdbPrefix.toNIO) &&
    PathIO.extension(path.toNIO) == semanticdbExtension
  }

  def toSource(path: RelativePath): RelativePath = {
    require(isSemanticdb(path))
    val sourceSibling = path.resolveSibling(_.stripSuffix(semanticdbExtension))
    semanticdbPrefix.relativize(sourceSibling)
  }

  def isSource(path: RelativePath): Boolean = {
    !isSemanticdb(path)
  }

  def fromSource(path: RelativePath): RelativePath = {
    require(isSource(path))
    val semanticdbSibling = path.resolveSibling(_ + "." + semanticdbExtension)
    semanticdbPrefix.resolve(semanticdbSibling)
  }
}
