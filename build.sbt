import com.github.eikek.sbt.openapi._
import scala.sys.process._
import com.typesafe.sbt.SbtGit.GitKeys._

val elmCompileMode = settingKey[ElmCompileMode]("How to compile elm sources")

val sharedSettings = Seq(
  organization := "com.github.eikek",
  version := "0.2.0-SNAPSHOT",
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-feature",
    "-Ypartial-unification",
    "-Xfatal-warnings", // fail when there are warnings
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import"
  ),
  scalacOptions in (Compile, console) := Seq()
)

val testSettings = Seq(
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  libraryDependencies ++= Dependencies.testing
)

val elmSettings = Seq(
  elmCompileMode := ElmCompileMode.Debug,
  Compile/resourceGenerators += (Def.task {
    compileElm(streams.value.log
      , (Compile/baseDirectory).value
      , (Compile/resourceManaged).value
      , name.value
      , version.value
      , elmCompileMode.value)
  }).taskValue,
  watchSources += Watched.WatchSource(
    (Compile/sourceDirectory).value/"elm"
      , FileFilter.globFilter("*.elm")
      , HiddenFileFilter
  )
)

val webjarSettings = Seq(
  Compile/resourceGenerators += (Def.task {
    copyWebjarResources(Seq((sourceDirectory in Compile).value/"webjar", (Compile/resourceDirectory).value/"openapi.yml")
      , (Compile/resourceManaged).value
      , name.value
      , version.value
      , streams.value.log
    )
  }).taskValue,
  Compile/sourceGenerators += (Def.task {
    createWebjarSource(Dependencies.webjars, (Compile/sourceManaged).value)
  }).taskValue,
  Compile/unmanagedResourceDirectories ++= Seq((Compile/resourceDirectory).value.getParentFile/"templates"),
  watchSources += Watched.WatchSource(
    (Compile / sourceDirectory).value/"webjar"
      , FileFilter.globFilter("*.js") || FileFilter.globFilter("*.css")
      , HiddenFileFilter
  )
)

val debianSettings = Seq(
  maintainer := "Eike Kettner <eike.kettner@posteo.de>",
  packageSummary := description.value,
  packageDescription := description.value,
  mappings in Universal += {
    val conf = (Compile / resourceDirectory).value / "reference.conf"
    if (!conf.exists) {
      sys.error(s"File $conf not found")
    }
    conf -> "conf/attentive.conf"
  },
  bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/attentive.conf""""
)

lazy val root = (project in file(".")).
  enablePlugins(OpenApiSchema
    , BuildInfoPlugin
    , JavaServerAppPackaging
    , DebianPlugin
    , SystemdPlugin).
  settings(sharedSettings).
  settings(testSettings).
  settings(elmSettings).
  settings(webjarSettings).
  settings(debianSettings).
  settings(
    name := "attentive",
    description := "Personal audio scrobbler service.",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Dependencies.http4s ++
      Dependencies.circe ++
      Dependencies.fastparse ++
      Dependencies.pureconfig ++
      Dependencies.logging ++
      Dependencies.yamusca ++
      Dependencies.databases ++
      Dependencies.doobie ++
      Dependencies.flyway ++
      Dependencies.webjars,
    javaOptions in reStart := (javaOptions in run).value  ++ Seq(s"-Dconfig.file=${baseDirectory.value/"dev.conf"}"),
    addCompilerPlugin(Dependencies.kindProjectorPlugin),
    addCompilerPlugin(Dependencies.betterMonadicFor),
    openapiTargetLanguage := Language.Scala,
    openapiPackage := Pkg("attentive.web.model"),
    openapiSpec := (Compile/resourceDirectory).value/"openapi.yml",
    openapiScalaConfig := ScalaConfig().withJson(ScalaJson.circeSemiauto),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, gitHeadCommit, gitHeadCommitDate, gitUncommittedChanges, gitDescribedVersion),
    buildInfoPackage := "attentive",
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime
  )

def copyWebjarResources(src: Seq[File], base: File, artifact: String, version: String, logger: Logger): Seq[File] = {
  val targetDir = base/"META-INF"/"resources"/"webjars"/artifact/version
  src.flatMap { dir =>
    if (dir.isDirectory) {
      val files = (dir ** "*").filter(_.isFile).get pair Path.relativeTo(dir)
      files.map { case (f, name) =>
        val target = targetDir/name
        logger.info(s"Copy $f -> $target")
        IO.createDirectories(Seq(target.getParentFile))
        IO.copy(Seq(f -> target))
        target
      }
    } else {
      val target = targetDir/dir.name
      logger.info(s"Copy $dir -> $target")
      IO.createDirectories(Seq(target.getParentFile))
      IO.copy(Seq(dir -> target))
      Seq(target)
    }
  }
}

def compileElm(logger: Logger, wd: File, outBase: File, artifact: String, version: String, mode: ElmCompileMode): Seq[File] = {
  logger.info("Compile elm files ...")
  val target = outBase/"META-INF"/"resources"/"webjars"/artifact/version/"attentive-app.js"
  val cmd = Seq("elm", "make") ++ mode.flags ++ Seq("--output", target.toString)
  val proc = Process(cmd ++ Seq(wd/"src"/"main"/"elm"/"Main.elm").map(_.toString), Some(wd))
  val out = proc.!!
  logger.info(out)
  Seq(target)
}

def createWebjarSource(wj: Seq[ModuleID], out: File): Seq[File] = {
  val target = out/"Webjars.scala"
  val fields = wj.map(m => s"""val ${m.name.toLowerCase.filter(_ != '-')} = "/${m.name}/${m.revision}" """).mkString("\n\n")
  val content = s"""package attentive.web
    |object Webjars {
    |$fields
    |}
    |""".stripMargin

  IO.write(target, content)
  Seq(target)
}

addCommandAlias("make", ";set root/elmCompileMode := ElmCompileMode.Production ;root/openapiCodegen ;root/test:compile")
addCommandAlias("make-zip", ";root/universal:packageBin")
addCommandAlias("make-deb", ";root/debian:packageBin")
addCommandAlias("make-pkg", ";clean ;make ;make-zip ;make-deb")
