# scala.today

Copied from https://github.com/VirtusLab/scala.today/tree/main/app.

scala.today is a website for discovering Scala community content.

## Changes from upstream

- Removed `main.scala` (requires Java 21+ for OxApp)
- Added `config.scala` with Config case class
- Commented out functions in `scraping.scala` that require Java 21+ (using Ox context)
- Commented out `OxApp` trait and `sleepAfter` in `util.scala`
- Fixed fastparse context parameter syntax for Scala 3.7+ compatibility
- Changed `classOf[App]` to `classOf[Http]` to avoid deprecated scala.App reference

Tests can be run via `sbt "benchScalaToday/runMain scala.today.SemanticVersionTest"` (if a test runner is set up).
