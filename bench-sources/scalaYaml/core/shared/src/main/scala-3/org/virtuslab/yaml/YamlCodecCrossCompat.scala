package org.virtuslab.yaml

import scala.deriving.Mirror

private[yaml] trait YamlCodecCompanionCrossCompat {

  def make[A](using decoder: YamlDecoder[A], encoder: YamlEncoder[A]): YamlCodec[A]

  inline def derived[T](using m: Mirror.Of[T]): YamlCodec[T] =
    val decoder = YamlDecoder.derived[T]
    val encoder = YamlEncoder.derived[T]
    make(using decoder, encoder)
}
