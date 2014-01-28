import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "OneWebPro-scala-play-bootstrap"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe.slick" %% "slick" % "2.0.0",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.play" %% "play-slick_2.10" % "0.6.0-SNAPSHOT",
    "org.imgscalr" % "imgscalr-lib" % "4.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
  )

}