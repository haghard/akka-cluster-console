import sbt._
import com.typesafe.sbt.web.SbtWeb
import sbtdocker.ImageName
import scala.sys.process.Process

val scalaV = "2.12.8"
val akkaVersion = "2.5.23"
val version = "0.0.2"
val ReactJs = "16.2.0"

lazy val server = (project in file("server")).settings(
  name := "server",

  resolvers ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
    "isarn project" at "https://dl.bintray.com/isarn/maven/"
  ),

  scalacOptions in(Compile, console) := Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked"),
  scalaVersion             := scalaV,
  scalaJSProjects          := Seq(ui),
  pipelineStages in Assets := Seq(scalaJSPipeline),

  compile in Compile := (compile in Compile).dependsOn(scalaJSPipeline, cpCss).value,
  scalafmtOnCompile := true,

  //javaOptions in runMain := Seq("ENV=development", "CONFIG=./server/conf"),

  fork in runMain := true,
  fork in run := true,

  libraryDependencies ++= Seq(
    //"com.typesafe.play.extras" %% "play-geojson" % "1.4.0",
    "ch.qos.logback"  %   "logback-classic" % "1.1.2",
    "org.mindrot"     %   "jbcrypt"         % "0.4",
    "org.webjars"     %   "bootstrap"       % "3.3.6",
    "com.lihaoyi"     %%  "scalatags"       % "0.6.5",
    "com.jsuereth"    %%  "scala-arm"       % "2.0",
    "org.scalatest"   %%  "scalatest"       % "3.0.8" % "test"
  ) ++ Seq(
    "com.softwaremill.akka-http-session" %% "core" % "0.5.6",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    //"com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
    //"com.typesafe.akka" %% "akka-persistence-cassandra" % "0.98"
  ),

  //javaOptions in runMain += "-DENV=prod",

  //exclude("com.typesafe.akka", "akka-http")

  /*
  javaOptions in Universal ++= Seq(
    // -J params will be added as jvm parameters
    "-J-Xmn1G",
    "-J-Xms1G",
    "-J-Xmx3G",
    // others will be added as app parameters
    "-Dlog4j.configurationFile=conf/log4j2.xml"
  ),
*/

  WebKeys.packagePrefix in Assets := "public/",

  (managedClasspath in Runtime) += (packageBin in Assets).value,

  mainClass in assembly := Some("console.Application"),

  assemblyJarName in assembly := s"akka-cluster-console-${version}.jar",

  // Resolve duplicates for Sbt Assembly
  assemblyMergeStrategy in assembly := {
    case PathList(xs@_*) if xs.last == "io.netty.versions.properties" => MergeStrategy.rename
    case other => (assemblyMergeStrategy in assembly).value(other)
  },

  imageNames in docker := Seq(
    ImageName(namespace = Some("haghard"), repository = "cluster-console",
    tag = Some(version))),

  buildOptions in docker := BuildOptions(cache = false,
    removeIntermediateContainers = BuildOptions.Remove.Always,
    pullBaseImage = BuildOptions.Pull.Always),

  //envVars := Map("-DENV" -> "development", "-DCONFIG" -> "./server/conf"),

  //sbt -Denv=...  -Dconfig=...
  /*
  envVars in runMain := Map(
    "ENV" -> sys.props.getOrElse("env", "development"),
    "CONFIG" -> sys.props.getOrElse("config", "./server/conf")
  ),
  */

  //sbt docker
  //sbt -Denv="development" && docker
  dockerfile in docker := {
    //development | production
    val appEnv = sys.props.getOrElse("env", "production")
    //Option(System.getenv("env")).getOrElse("production")
    //sys.props.getOrElse("env", "production")
    println(s"★ ★ ★ ★ ★ ★ Build Docker image for Env:$appEnv ★ ★ ★ ★ ★ ★")

    //val appConfig = "/app/conf"
    val baseDir = baseDirectory.value
    val artifact: File = assembly.value

    val imageAppBaseDir = "/app"
    val configDir = "conf"
    val artifactTargetPath = s"$imageAppBaseDir/${artifact.name}"
    val artifactTargetPath_ln = s"$imageAppBaseDir/${appEnv}-${name.value}.jar"
    val jksTargetPath = s"$imageAppBaseDir/haghard.jks"

    val dockerResourcesDir = baseDir / "docker-resources"
    val dockerResourcesTargetPath = s"$imageAppBaseDir/"

    val jks = baseDir / "ssl" / "haghard.jks"

    //val prodConfigSrc = baseDir / "src" / "main" / "resources" / "production.conf"

    val prodConfigSrc = baseDir / "conf" / "production.conf"
    val devConfigSrc =  baseDir / "conf" / "development.conf"

    //val curEnv = System.getenv("ENV")

    val appProdConfTarget = s"$imageAppBaseDir/$configDir/production.conf"
    val appDevConfTarget = s"$imageAppBaseDir/$configDir/development.conf"

    new sbtdocker.mutable.Dockerfile {
      from("adoptopenjdk/openjdk12")
      //from("openjdk:10-jre")
      //from("openjdk:9-jre")
      maintainer("haghard")

      env("VERSION", version)
      env("APP_BASE", imageAppBaseDir)
      env("CONFIG", s"$imageAppBaseDir/$configDir")

      env("ENV", appEnv)

      //workDir(imageAppBaseDir)
      //run("mkdir", configDir)

      workDir(imageAppBaseDir)

      copy(artifact, artifactTargetPath)
      copy(dockerResourcesDir, dockerResourcesTargetPath)
      copy(jks, jksTargetPath)

      if(prodConfigSrc.exists)
        copy(prodConfigSrc, appProdConfTarget) //Copy the prod config

      if(devConfigSrc.exists)
        copy(devConfigSrc, appDevConfTarget) //Copy the prod config

      runRaw(s"ls $appProdConfTarget")
      runRaw(s"ls $appDevConfTarget")

      runRaw(s"cd $configDir && ls -la && cd ..")
      runRaw("ls -la")

      //Symlink the service jar to a non version specific name
      run("ln", "-sf", s"$artifactTargetPath", s"$artifactTargetPath_ln")

      entryPoint(s"${dockerResourcesTargetPath}docker-entrypoint.sh")
    }
  }
).enablePlugins(SbtWeb, sbtdocker.DockerPlugin, BuildInfoPlugin)
  .dependsOn(sharedJvm)

//for debugging
def cpCss() = (baseDirectory) map { dir =>
  def execute() = {
    Process(s"cp ${dir}/src/main/resources/d3.v3.min.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/d3.v4.min.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    Process(s"cp ${dir}/src/main/resources/queue.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/colorbrewer.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    Process(s"cp ${dir}/src/main/resources/web/linked-charts/area1.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/web/linked-charts/area2.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
  }

  println("Coping resources ...")
  haltOnCmdResultError(execute())
}

def haltOnCmdResultError(result: Int) {
  if (result != 0) throw new Exception("Build failed")
}

lazy val ui = (project in file("ui")).settings(
  resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  scalaVersion := scalaV,
  scalafmtOnCompile := true,

  libraryDependencies ++= Seq(
    "org.singlespaced" %%% "scalajs-d3" %     "0.3.4",

    /*"com.github.japgolly.scalajs-react" %%%   "core"      % "0.11.3",
    "com.github.japgolly.scalajs-react" %%%   "extra"     % "0.11.3",*/

    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2",

    "com.github.japgolly.scalacss"      %%%   "ext-react" % "0.5.6"
  ),

  /*jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js",

    "org.webjars.bower" % "react" % "15.4.2"
        /        "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",

      "org.webjars.bower" % "react" % "15.4.2"
        /         "react-dom.js"
        minified  "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",

      "org.webjars.bower" % "react" % "15.4.2"
        /         "react-dom-server.js"
        minified  "react-dom-server.min.js"
        dependsOn "react-dom.js"
        commonJSName "ReactDOMServer"
  ),*/


  jsDependencies ++= Seq(
      "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js",

      "org.webjars.npm" % "react" % ReactJs
        / "umd/react.development.js"
        minified "umd/react.production.min.js"
        commonJSName "React",

      "org.webjars.npm" % "react-dom" % ReactJs
        / "umd/react-dom.development.js"
        minified "umd/react-dom.production.min.js"
        dependsOn "umd/react.development.js"
        commonJSName "ReactDOM",

      "org.webjars.npm" % "react-dom" % ReactJs
        / "umd/react-dom-test-utils.development.js"
        minified "umd/react-dom-test-utils.production.min.js"
        dependsOn "umd/react-dom.development.js"
        commonJSName "ReactTestUtils",

      "org.webjars.npm" % "react-dom" % ReactJs
        / "umd/react-dom-server.browser.development.js"
        minified "umd/react-dom-server.browser.production.min.js"
        dependsOn "umd/react-dom.development.js"
        commonJSName "ReactDOMServer"
    ),

    dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2",


  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }

).enablePlugins(ScalaJSPlugin, ScalaJSWeb).dependsOn(sharedJs)

lazy val shared = sbtcrossproject.CrossPlugin.autoImport.crossProject(JSPlatform, JVMPlatform)
  .crossType(sbtcrossproject.CrossType.Pure)
  .settings(
    scalaVersion := scalaV,
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.6.6",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      "me.chrons" %%% "boopickle" % "1.2.5"
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
//onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
//onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}

//cancelable in Global := true