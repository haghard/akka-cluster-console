import sbt.*
import com.typesafe.sbt.web.SbtWeb
import sbtdocker.ImageName

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.sys.process.Process

val scalaV          = "2.13.1"
val akkaVersion     = "2.6.21"
val version         = "0.1.0"
val AkkaMngVersion  = "1.4.1"
val AkkaHttpVersion = "10.2.10"

val scalaOps = Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked")

//scalaVersion := scalaV

lazy val server = (project in file("server"))
  .settings(
    name := "server",
    resolvers ++= Seq("Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"),
    scalaVersion := scalaV,
    scalaJSProjects := Seq(ui),
    Assets / pipelineStages := Seq(scalaJSPipeline),

    Compile / scalacOptions := scalaOps,
    console / scalacOptions := scalaOps,
    Compile / compile := (Compile / compile).dependsOn(scalaJSPipeline, copyJsArtifacts).value,
    scalafmtOnCompile := true,

    run / fork := true,
    run / connectInput := true,

    //For ammonite project server;test:run to work
    //runMain / fork := true,
    //run / fork := true,

    libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "org.webjars"    % "bootstrap"       % "3.3.6",
        "com.lihaoyi"    %% "scalatags"      % "0.9.1"
        //"pl.setblack"     %%  "cryptotpyrc"     % "0.4.3",
      ) ++ Seq(
        "com.typesafe.akka"             %% "akka-http"                         % AkkaHttpVersion,
        "ch.megard"                     %% "akka-http-cors"                    % "1.2.0",
        "com.typesafe.akka"             %% "akka-slf4j"                        % akkaVersion,
        "com.typesafe.akka"             %% "akka-stream"                       % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster-metrics"              % akkaVersion,
        "com.typesafe.akka"             %% "akka-discovery"                    % akkaVersion,
        "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaMngVersion,
        "com.lightbend.akka.management" %% "akka-management-cluster-http"      % AkkaMngVersion,
        ("com.lihaoyi" % "ammonite" % "2.5.0" % "test").cross(CrossVersion.full)
      ),
    dependencyOverrides ++= Seq(
        "com.typesafe.akka"             %% "akka-actor-typed"                  % akkaVersion,
        "com.typesafe.akka"             %% "akka-protobuf"                     % akkaVersion,
        "com.typesafe.akka"             %% "akka-protobuf-v3"                  % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster-sharding"             % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster-metrics"              % akkaVersion,
        "com.typesafe.akka"             %% "akka-slf4j"                        % akkaVersion,
        "com.typesafe.akka"             %% "akka-discovery"                    % akkaVersion,
        "com.typesafe.akka"             %% "akka-distributed-data"             % akkaVersion,
        "com.typesafe.akka"             %% "akka-persistence"                  % akkaVersion,
        "com.typesafe.akka"             %% "akka-persistence-query"            % akkaVersion,
        "com.typesafe.akka"             %% "akka-persistence-typed"            % akkaVersion,
        "com.typesafe.akka"             %% "akka-actor"                        % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster"                      % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster-sharding-typed"       % akkaVersion,
        "com.typesafe.akka"             %% "akka-coordination"                 % akkaVersion,
        "com.typesafe.akka"             %% "akka-stream"                       % akkaVersion,
        "com.typesafe.akka"             %% "akka-cluster-tools"                % akkaVersion,
        "com.typesafe.akka"             %% "akka-http"                         % AkkaHttpVersion,
        "com.typesafe.akka"             %% "akka-http-core"                    % AkkaHttpVersion,
        "com.typesafe.akka"             %% "akka-http-spray-json"              % AkkaHttpVersion,
        "com.typesafe.akka"             %% "akka-discovery"                    % akkaVersion,
        "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaMngVersion,
        "com.lightbend.akka.management" %% "akka-management-cluster-http"      % AkkaMngVersion
      ),
    
    javaOptions ++= Seq(
      "-XX:+PrintCommandLineFlags",
      "-XshowSettings:system",
      "-Xms128m",
      "-Xmx256m",
      "-XX:-UseAdaptiveSizePolicy",   //heap never resizes
      "-XX:MaxDirectMemorySize=128m", //Will get a error if allocate more mem for direct byte buffers
      "-XX:+UseParallelGC",  //with heaps <4GB
      //"-XX:+UseG1GC",  //with heaps >4GB
      //"-XX:+UseZGC",  //apps that require sub-millisecond GC pauses, with gigantic (terabyte range) heaps
      //https://softwaremill.com/reactive-event-sourcing-benchmarks-part-2-postgresql/
      "-XX:ActiveProcessorCount=2",
    ),

    Assets / WebKeys.packagePrefix := "public/",
    (Runtime / managedClasspath) += (Assets / packageBin).value,
    assembly / mainClass := Some("console.Application"),
    assembly / assemblyJarName := s"akka-cluster-console-$version.jar",

    // Resolve duplicates for Sbt Assembly
    assembly / assemblyMergeStrategy := {
      case PathList(xs @ _*) if xs.last == "io.netty.versions.properties" ⇒ MergeStrategy.rename
      case other                                                          ⇒ (assembly / assemblyMergeStrategy).value(other)
    },

    docker / imageNames := Seq(
        ImageName(namespace = Some("haghard"), repository = "cluster-console", tag = Some(version))
      ),
    docker / buildOptions := BuildOptions(
        cache = false,
        removeIntermediateContainers = BuildOptions.Remove.Always,
        pullBaseImage = BuildOptions.Pull.Always
      ),
    //test:run
    Test / sourceGenerators += Def.task {
        val file = (Test / sourceManaged).value / "amm.scala"
        IO.write(file, """object amm extends App { ammonite.Main().run() }""")
        Seq(file)
      }.taskValue,
    docker / dockerfile := {
      //development | production
      val appEnv = sys.props.getOrElse("env", "production")
      println(s"★ ★ ★ ★ ★ ★ Build Docker image for Env:$appEnv ★ ★ ★ ★ ★ ★")

      //val appConfig = "/app/conf"
      val baseDir        = baseDirectory.value
      val artifact: File = assembly.value

      val imageAppBaseDir       = "/app"
      val configDir             = "conf"
      val artifactTargetPath    = s"$imageAppBaseDir/${artifact.name}"
      val artifactTargetPath_ln = s"$imageAppBaseDir/${appEnv}-${name.value}.jar"

      val dockerResourcesDir        = baseDir / "docker-resources"
      val dockerResourcesTargetPath = s"$imageAppBaseDir/"

      val prodConfigSrc = baseDir / "conf" / "production.conf"
      val devConfigSrc  = baseDir / "conf" / "development.conf"

      //val curEnv = System.getenv("ENV")

      val appProdConfTarget = s"$imageAppBaseDir/$configDir/production.conf"
      val appDevConfTarget  = s"$imageAppBaseDir/$configDir/development.conf"

      new sbtdocker.mutable.Dockerfile {
        from("adoptopenjdk/openjdk11")
        //from("openjdk:10-jre")
        //from("openjdk:9-jre")
        maintainer("haghard")

        env("VERSION", version)
        env("APP_BASE", imageAppBaseDir)
        env("CONFIG", s"$imageAppBaseDir/$configDir")
        env("ENV", appEnv)

        workDir(imageAppBaseDir)
        copy(artifact, artifactTargetPath)
        copy(dockerResourcesDir, dockerResourcesTargetPath)

        if (prodConfigSrc.exists)
          copy(prodConfigSrc, appProdConfTarget) //Copy the prod config

        if (devConfigSrc.exists)
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
  )
  .enablePlugins(SbtWeb, sbtdocker.DockerPlugin, BuildInfoPlugin)
  .dependsOn(sharedJvm)

def haltOnCmdResultError(result: Int) {
  if (result != 0) throw new Exception("Build failed")
}

lazy val ui = (project in file("ui"))
  .settings(
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    scalaVersion := scalaV,
    scalafmtOnCompile := true,

    //resolvers += "jitpack" at "https://jitpack.io"
    //libraryDependencies += "com.github.fdietze.scala-js-d3v4" %%% "scala-js-d3v4" % "809f086"

    libraryDependencies ++= Seq(
        "org.singlespaced"                  %%% "scalajs-d3" % "0.4.0",//"0.4.0" local build
        "com.github.japgolly.scalajs-react" %%% "core"       % "1.5.0",// "0.11.5",
        "com.github.japgolly.scalajs-react" %%% "extra"      % "1.5.0",//"0.11.5",
        "com.github.japgolly.scalacss"      %%% "ext-react"  % "0.6.0"
        //"pl.setblack"                       %%%   "cryptotpyrc" % "0.4.3",
      ),
    //"2.1.4"
    jsDependencies ++= Seq(
        "org.webjars"       % "jquery" % "3.7.1" / "3.7.1/jquery.js",
        "org.webjars.bower" % "react"  % "15.4.2"
        / "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",
        "org.webjars.bower" % "react" % "15.4.2"
        / "react-dom.js"
        minified "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",
        "org.webjars.bower" % "react" % "15.4.2"
        / "react-dom-server.js"
        minified "react-dom-server.min.js"
        dependsOn "react-dom.js"
        commonJSName "ReactDOMServer"
      ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    }
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val shared =
  sbtcrossproject.CrossPlugin.autoImport
    .crossProject(JSPlatform, JVMPlatform)
    .crossType(sbtcrossproject.CrossType.Pure)
    .settings(
      scalaVersion := scalaV,
      scalafmtOnCompile := true,
      libraryDependencies ++= Seq("com.lihaoyi" %%% "upickle" % "1.0.0"), //"0.6.6"
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", xs @ _*) => MergeStrategy.discard
        case _                             => MergeStrategy.first
      }
    )
    .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs  = shared.js

//cancelable in Global := true

scalafmtOnCompile := true


ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
Global / semanticdbEnabled := true
Global / semanticdbVersion := scalafixSemanticdb.revision
Global / watchAntiEntropy := FiniteDuration(5000, TimeUnit.MILLISECONDS)


addCommandAlias("sfix", "scalafix OrganizeImports; test:scalafix OrganizeImports")
addCommandAlias("sFixCheck", "scalafix --check OrganizeImports; test:scalafix --check OrganizeImports")
addCommandAlias("c", "compile")
addCommandAlias("r", "reload")


def execute(dir: File): Unit = {
  Process(s"mkdir ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/js").!
  Process(s"mkdir ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css").!
  Process(s"cp ${dir}/src/main/resources/react/main.css ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css").!
  Process(s"cp ${dir}/src/main/resources/react/chat.css ${dir}/target/web/web-modules/main/webjars/lib/bootstrap/css").!
  val ec = Process(s"cp ${dir}/src/main/resources/akka-small.jpg  ${dir}/target").!
  if(ec != 0) throw new Exception("Copy error")
}

def copyJsArtifacts = baseDirectory.map { dir =>
  println("Update js resources ...")
  execute(dir)
}


addCommandAlias("fmt", "scalafmt")
addCommandAlias("c", "compile")
addCommandAlias("r", "reload")
