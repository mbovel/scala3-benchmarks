package sourcecode


object ManualImplicit {
  def apply() = {
    assert(foo() == "apply")
    assert(foo()(using "cow") == "cow")
    assert(bar() == 8)
    assert(bar()(using 123) == 123)
    assert(bar()(using 123) == 123)
    assert(baz() == "sourcecode.ManualImplicit.apply")
    assert(baz() == "sourcecode.ManualImplicit.apply")
    def enc() =
      assert(qux() == "sourcecode.ManualImplicit.apply enc")

    enc()
    def enc2() =
      assert(
        qux()(using "sourcecode.ManualImplicit")
        == "sourcecode.ManualImplicit"
      )

    enc2()
  }
  def foo()(using i: sourcecode.Name) = i.value
  def bar()(using i: sourcecode.Line) = i.value
  def baz()(using i: sourcecode.FullName) = i.value
  def qux()(using i: sourcecode.Enclosing) = i.value
}
