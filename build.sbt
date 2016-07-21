name := "literal-types"
organization := "io.github.jeremyrsmith"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
homepage := Some(url("http://github.com/jeremyrsmith/literal-types"))

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.0-M4")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "com.chuusai" %% "shapeless" % "2.3.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

// scalac options

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
  "-deprecation",
  "-unchecked"
)

scalacOptions in console in Compile += "-Xplugin:" + (packageBin in Compile).value

scalacOptions in Test += "-Xplugin:" + (packageBin in Compile).value

scalacOptions in Test += "-Yrangepos"

bintrayRepository := "maven"
bintrayVcsUrl := Some("https://github.com/jeremyrsmith/literal-types")


