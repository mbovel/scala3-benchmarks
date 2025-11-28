package dotty.tools.dotc.util

/** Forward-ported from the explicit-nulls branch. */
extension [T](x: T | Null)
  /** Should be used when we know from the context that `x` is not null.
   *  Flow-typing under explicit nulls will automatically insert many necessary
   *  occurrences of uncheckedNN.
   */
  transparent inline def uncheckedNN: T = x.asInstanceOf[T]
