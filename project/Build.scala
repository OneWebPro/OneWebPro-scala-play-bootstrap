import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "OneWebPro-scala-play-bootstrap"
  val appVersion = "1.1.0"

  val appDependencies = Seq(
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.typesafe.play" %% "play-slick" % "0.8.1",
    "org.imgscalr" % "imgscalr-lib" % "4.2",
    "net.coobird" % "thumbnailator" % "0.4.8",
    "org.scalaz" %% "scalaz-core" % "7.0.7",
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    scalaVersion := "2.11.1",
    version := appVersion,
    libraryDependencies ++= appDependencies
    //		resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
  )

}