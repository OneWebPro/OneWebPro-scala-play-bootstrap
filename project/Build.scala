import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

	val appName = "OneWebPro-scala-play-bootstrap"
	val appVersion = "1.0-SNAPSHOT"

	val appDependencies = Seq(
		"com.typesafe.slick" %% "slick" % "1.0.0",
		"com.typesafe.play" %% "play-slick" % "0.6.0.0",
		"org.imgscalr" % "imgscalr-lib" % "4.2"
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(
//		resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
	)

}