package star.meta.internal.inputs {
  trait Api extends star.meta.inputs.Api
  trait Aliases extends star.meta.inputs.Aliases
}

package scala.meta.inputs {
  private[meta] trait Api extends star.meta.internal.inputs.Api
  private[meta] trait Aliases extends star.meta.internal.inputs.Aliases
}
