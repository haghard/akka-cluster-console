import _root_.sbtdocker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt._
import com.typesafe.sbt.packager.docker.Dockerfile
import com.typesafe.sbt.web.SbtWeb
import sbtdocker.ImageName

val scalaV = "2.12.1"
val akkaVersion = "2.4.17"
val version = "0.0.1"

scalaVersion := scalaV

name := "akka-cluster-console"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

updateOptions in Global := updateOptions.in(Global).value.withCachedResolution(true)

lazy val server = (project in file("server")).settings(
  resolvers ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
    "isarn project" at "https://dl.bintray.com/isarn/maven/"
  ),

  scalacOptions in(Compile, console) := Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked"),
  scalaVersion := scalaV,
  scalaJSProjects := Seq(ui),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile).dependsOn(scalaJSPipeline, cpCss),

  name := "server",

  //javaOptions in runMain := Seq("ENV=development", "CONFIG=./server/conf"),

  fork in runMain := true,

  libraryDependencies ++= Seq(
    //"com.typesafe.play.extras" %% "play-geojson" % "1.4.0",
    "ch.qos.logback"  %   "logback-classic" % "1.1.2",
    "org.mindrot"     %   "jbcrypt"         % "0.3m",
    "org.webjars"     %   "bootstrap"       % "3.3.6",
    "com.lihaoyi"     %%  "scalatags"       % "0.6.2",
    "com.jsuereth"    %%  "scala-arm"       % "2.0",
    "org.scalatest"   %%  "scalatest"       % "3.0.1" % "test"
  ) ++ Seq(
    "com.softwaremill.akka-http-session" %% "core" % "0.4.0",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    //"com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    //"com.typesafe.akka" %% "akka-persistence-cassandra" % "0.22",
    //"com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    //"com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion
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
    val jksTargetPath = s"$imageAppBaseDir/linguistic.jks"

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
      from("openjdk:8-jre")
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
).enablePlugins(SbtWeb, JavaAppPackaging, sbtdocker.DockerPlugin).dependsOn(sharedJvm)

//for debugging
def cpCss() = (baseDirectory) map { dir =>
  def execute() = {
    //IO.copyFile(dir / "src" / "main" / "twirl" / "linguistic"/ "main.css", dir / "target" /"web"/"web-modules"/"main"/"webjars"/"lib"/"bootstrap"/"css"/"main.css")
    //IO.copyFile(dir /"src"/"main"/"resources"/"chat.css", dir/"target"/"web"/"web-modules"/"main"/"webjars"/"lib"/"bootstrap"/"css"/"chat.css")

    //Process(s"cp ${dir}/src/main/resources/nv.d3.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    //Process(s"cp ${dir}/src/main/resources/nv.d3.css ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css/").!
    //Process(s"cp ${dir}/src/main/resources/graph/graph.css ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css/").!
    //Process(s"cp ${dir}/src/main/resources/graph/graph.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    //Process(s"cp ${dir}/src/main/resources/nv.d3.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    Process(s"cp ${dir}/src/main/resources/d3.v3.min.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/d3.v4.min.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    Process(s"cp ${dir}/src/main/resources/queue.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/colorbrewer.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    Process(s"cp ${dir}/src/main/resources/web/linked-charts/area1.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    Process(s"cp ${dir}/src/main/resources/web/linked-charts/area2.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!

    //Process(s"cp ${dir}/src/main/resources/wordtree/raphael.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    //Process(s"cp ${dir}/src/main/resources/wordtree/word-tree-layout.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    //Process(s"cp ${dir}/src/main/resources/wordtree/wordtree.js ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
    //Process(s"cp ${dir}/src/main/resources/tree.css ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css/").!
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
  persistLauncher := true,
  persistLauncher in Test := false,

  libraryDependencies ++= Seq(
    "org.singlespaced" %%% "scalajs-d3" % "0.3.4", //the version for scala 2.12 is 0.3.4
    "com.github.japgolly.scalajs-react" %%%   "core"      % "0.11.3",
    "com.github.japgolly.scalajs-react" %%%   "extra"     % "0.11.3",
    "com.github.japgolly.scalacss"      %%%   "ext-react" % "0.5.1"
  ),

  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js",

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
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.4.4",
      "com.lihaoyi" %%% "autowire" % "0.2.6",
      "me.chrons" %%% "boopickle" % "1.2.5"
    )
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

//cancelable in Global := true