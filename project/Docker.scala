import sbt.Keys._
import sbt._
import sbtdocker.DockerPlugin.autoImport._

object Docker {

  val settings = Seq(
    dockerfile in docker := {
      val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value

      val classpath = (managedClasspath in Compile).value
      val jarTarget = s"/deployments/${jarFile.getName}"

      val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
      val classpathString = classpath.files.map("/deployments/" + _.getName)
        .mkString(":") + ":" + jarTarget
      new Dockerfile {
        from("fabric8/java-alpine-openjdk8-jdk")

        add(classpath.files, "/deployments/")
        add(jarFile, s"/deployments/${jarFile.getName}")

        env("JAVA_APP_DIR", "/deployments")
        env("JAVA_APP_JAR", s"/deployments/${jarFile.getName}")
        env("AB_OFF", "1")
        env("JAVA_MAIN_CLASS", mainclass)
        env("JAVA_CLASSPATH", classpathString)

        entryPointRaw("/deployments/run-java.sh")
      }
    },
    imageNames in docker := {
      Seq(
        ImageName(
          registry = Some("registry.gitlab.com"),
          repository = s"tomaszym/${name.in(ProjectRef(file("."), "root")).value}/${name.value}:latest"
        )
      )
    }
  )
}
