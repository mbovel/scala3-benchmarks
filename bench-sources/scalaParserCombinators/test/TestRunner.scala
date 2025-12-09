package scala.util.parsing.combinator.test

import org.junit.runner.JUnitCore
import org.junit.runner.notification.{Failure, RunListener}

object TestRunner:
  def main(args: Array[String]): Unit =
    val junit = new JUnitCore()

    var failures = 0
    var runs = 0

    junit.addListener(new RunListener {
      override def testFailure(failure: Failure): Unit =
        println(s"FAILED: ${failure.getDescription}")
        println(s"  ${failure.getMessage}")
        if failure.getTrace != null then
          failure.getTrace.split("\n").take(5).foreach(line => println(s"  $line"))

      override def testFinished(description: org.junit.runner.Description): Unit =
        runs += 1
    })

    val testClasses: Array[Class[?]] = Array(
      classOf[scala.util.parsing.combinator.JavaTokenParsersTest],
      classOf[scala.util.parsing.combinator.LongestMatchTest],
      classOf[scala.util.parsing.combinator.PackratParsersTest],
      classOf[scala.util.parsing.combinator.RegexParsersTest],
      classOf[scala.util.parsing.combinator.gh242],
      classOf[scala.util.parsing.combinator.gh29],
      classOf[scala.util.parsing.combinator.gh45],
      classOf[scala.util.parsing.combinator.gh56],
      classOf[scala.util.parsing.combinator.gh72],
      classOf[scala.util.parsing.combinator.T0700],
      classOf[scala.util.parsing.combinator.T1100],
      classOf[scala.util.parsing.combinator.t1229],
      classOf[scala.util.parsing.combinator.t3212],
      classOf[scala.util.parsing.combinator.T4138],
      classOf[scala.util.parsing.combinator.T5514],
      classOf[scala.util.parsing.combinator.t5669],
      classOf[scala.util.parsing.combinator.t6067],
      classOf[scala.util.parsing.combinator.t6464],
      classOf[scala.util.parsing.combinator.t7483],
      classOf[scala.util.parsing.combinator.t8879],
      classOf[scala.util.parsing.combinator.lexical.StdLexicalTest],
      classOf[scala.util.parsing.input.OffsetPositionTest],
      classOf[scala.util.parsing.input.gh178],
      classOf[scala.util.parsing.input.gh64],
    )

    val result = junit.run(testClasses*)

    println()
    println(s"Tests: ${result.getRunCount}, Passed: ${result.getRunCount - result.getFailureCount}, Failed: ${result.getFailureCount}")

    if result.getFailureCount > 0 then
      result.getFailures.forEach { failure =>
        println(s"\nFailed: ${failure.getDescription}")
        println(s"Message: ${failure.getMessage}")
      }
      sys.exit(1)
