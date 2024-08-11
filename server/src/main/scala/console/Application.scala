package console

import java.io.File
import java.util.TimeZone
import java.time.LocalDateTime
import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection._
import scala.concurrent.duration.{FiniteDuration, NANOSECONDS}

object Application extends App with AppSupport {

  val HttpDispatcher = "http"

  val opts: Map[String, String] = argsToOpts(args.toList)
  applySystemProperties(opts)

  val confPath = Option(System.getProperty("CONFIG")).getOrElse(throw new Exception("CONFIG is expected"))
  val hostname = Option(System.getProperty("akka.remote.artery.canonical.hostname"))
    .getOrElse(throw new Exception("akka.remote.artery.canonical.hostname is expected"))
  val akkaPort =
    Option(System.getProperty("akka.remote.artery.canonical.port")).getOrElse(throw new Exception("Port is expected"))
  val url      = Option(System.getProperty("URL")).getOrElse(throw new Exception("URL is expected"))
  val httpPort = Option(System.getProperty("HTTP_PORT")).getOrElse(throw new Exception("HTTP_PORT is expected"))

  val env = Option(System.getProperty("ENV")).getOrElse(throw new Exception("ENV is expected"))
  val configFile =
    new File(s"${new File(confPath).getAbsolutePath}/" + env + ".conf")

  applySystemProperties(
    Map(
      "-Dakka.remote.artery.bind.hostname"   -> hostname,
      "-Dakka.remote.artery.bind.port"       -> akkaPort,
      "-Dakka.management.http.hostname"      -> hostname,
      "-Dakka.management.http.port"          -> (httpPort.toInt + 1).toString,
      "-Dakka.management.http.bind-hostname" -> hostname,
      "-Dakka.management.http.bind-port"     -> (httpPort.toInt + 1).toString
    )
  )

  val config: Config =
    ConfigFactory
      .parseFile(configFile)
      .resolve()
      .withFallback(ConfigFactory.load())

  config.getConfig(HttpDispatcher)

  implicit val system = ActorSystem("my-console", config)
  val runtimeInfo = new StringBuilder()
    .append('\n')
    .append(s"Cores:${Runtime.getRuntime.availableProcessors}")
    .append(" Total Memory:" + Runtime.getRuntime.totalMemory / 1000000 + "Mb")
    .append(" Max Memory:" + Runtime.getRuntime.maxMemory / 1000000 + "Mb")
    .append(" Free Memory:" + Runtime.getRuntime.freeMemory / 1000000 + "Mb")
    .append('\n')
    .append("=================================================================================================")

  val greeting = new StringBuilder()
    .append('\n')
    .append("=================================================================================================")
    .append('\n')
    .append(s"★ ★ ★  Environment: $env Config: ${configFile.getAbsolutePath}  ★ ★ ★")
    .append('\n')
    .append(s"★ ★ ★  TimeZone: ${TimeZone.getDefault.getID} Started at ${LocalDateTime.now}  ★ ★ ★")
    .append('\n')
    .append(s"★ ★ ★  ${hostname}:${httpPort} - ${url} ★ ★ ★")
    .append('\n')
    .append(
      """
            |  ___
            | / __|  ___   _ _  __ __  ___   _ _
            | \__ \ / -_) | '_| \ V / / -_) | '_|
            | |___/ \___| |_|    \_/  \___| |_|
            |
            |""".stripMargin
    )
    .append("=================================================================================================")

  system.log.info(greeting.toString)
  system.log.info(runtimeInfo.toString())

  Bootstrap(hostname, httpPort.toInt, url)

  akka.management.scaladsl.AkkaManagement(system).start()
  akka.management.cluster.bootstrap.ClusterBootstrap(system).start()

  val _ = scala.io.StdIn.readLine()
  system.log.warning("★ ★ ★ ★ ★ ★  Shutting down ... ★ ★ ★ ★ ★ ★")
  system.terminate()
  scala.concurrent.Await
    .result(
      system.whenTerminated,
      FiniteDuration(
        system.settings.config.getDuration("akka.coordinated-shutdown.default-phase-timeout").toNanos,
        NANOSECONDS
      )
    )
}
