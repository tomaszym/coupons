import sbt.file

val pureConfigVersion = "0.10.1"
val http4sVersion = "0.20.0-M5"
lazy val doobieVersion = "0.6.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",
  organization := "com.kompreneble",
  scalacOptions := Seq(
    "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation", "-feature", "-Xmacro-settings:materialize-derivations",
    "-unchecked", "-language:implicitConversions", "-language:postfixOps", "-Ypartial-unification", "-language:higherKinds"),
  javaOptions +="-Duser.timezone=GMT",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.3",
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats" % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,

    "com.beachape" %% "enumeratum" % "1.5.13",


    "ch.qos.logback" % "logback-classic" % "1.2.3",
//    "net.logstash.logback" % "logstash-logback-encoder" % "5.2",
    "io.chrisdavenport" %% "log4cats-extras"  % "0.2.0",
    "io.chrisdavenport" %% "log4cats-slf4j"   % "0.2.0",

    "org.typelevel" %% "cats-core" % "1.6.0",
    "org.typelevel" %% "cats-mtl-core" % "0.4.0",

    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalamock" %% "scalamock" % "4.1.0" % Test,
    "com.47deg" %% "scalacheck-toolbox-datetime" % "0.2.5" % Test,
    "io.chrisdavenport" %% "cats-scalacheck" % "0.1.0" % Test,

//    "org.typelevel" %% "cats-tagless-macros" % "0.1.0"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
//  addCompilerPlugin(("org.scalameta" % "paradise" % "3.0.0-M11").cross(CrossVersion.full))
)


lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "coupons"
  )
   .aggregate(http)

lazy val http = (project in file("http"))
  .settings(commonSettings)
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(Docker.settings)
  .enablePlugins(GitVersioning)
  .settings (
    name := "coupons-http",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,

      "io.chrisdavenport" %% "fuuid" % "0.2.0-M4",
      "io.chrisdavenport" %% "fuuid-circe" % "0.2.0-M4",
      "io.chrisdavenport" %% "fuuid-http4s" % "0.2.0-M4",
      "io.chrisdavenport" %% "fuuid-doobie" % "0.2.0-M4",

      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2"   % doobieVersion,

      "io.circe" %% "circe-generic-extras" % "0.11.1",
      "io.circe" %% "circe-derivation" % "0.11.0-M1",
    ),
    useJGit,
    git.useGitDescribe := true,
  )


