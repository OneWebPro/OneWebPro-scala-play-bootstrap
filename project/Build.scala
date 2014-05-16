import sbt._
import Keys._

object ApplicationBuild extends Build {

	val appName = "OneWebPro-scala-play-bootstrap"
	val appVersion = "1.0.3"

	val appDependencies = Seq(
		"com.typesafe.slick" %% "slick" % "2.0.1",
		"com.typesafe.play" %% "play-slick" % "0.6.0.1",
		"org.imgscalr" % "imgscalr-lib" % "4.2",
		"org.scalaz" %% "scalaz-core" % "7.0.5" ,
		"org.mindrot" % "jbcrypt" % "0.3m"
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(
//		resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)
	)

}