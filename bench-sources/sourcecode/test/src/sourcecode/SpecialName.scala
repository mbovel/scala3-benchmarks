package sourcecode

object SpecialName {

  def macroValRun() = {
    def keyword(using name: sourcecode.Name): String = name.value

    val `macro` = keyword

    assert(`macro` == "macro")
  }

}
