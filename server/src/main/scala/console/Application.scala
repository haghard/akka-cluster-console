package console

import java.io.File
import java.lang.management.ManagementFactory
import java.util.TimeZone
import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializerSettings
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection._

object Application extends App with AppSupport {

  val opts: Map[String, String] = argsToOpts(args.toList)
  applySystemProperties(opts)

  val confPath = System.getProperty("CONFIG")
  val hostname = System.getProperty("HOSTNAME")
  val password = System.getProperty("PASSWORD")
  val httpPort = System.getProperty("HTTP_PORT")

  val confDir = new File(confPath)

  val env        = Option(System.getProperty("ENV")).getOrElse(throw new Exception("ENV is expected"))
  val configFile = new File(s"${new File(confPath).getAbsolutePath}/" + env + ".conf")

  val config: Config =
    ConfigFactory
      .parseFile(configFile)
      .resolve()
      .withFallback(ConfigFactory.load())

  implicit val system: ActorSystem = ActorSystem("cluster-console", config)
  implicit val mat = akka.stream.ActorMaterializer(
    ActorMaterializerSettings
      .create(system)
      .withDispatcher(Bootstrap.HttpDispatcher)
  )(system)

  Bootstrap(hostname, httpPort.toInt, password)

  val memorySize = ManagementFactory.getOperatingSystemMXBean
    .asInstanceOf[com.sun.management.OperatingSystemMXBean]
    .getTotalPhysicalMemorySize
  val runtimeInfo = new StringBuilder()
    .append('\n')
    .append(s"Cores:${Runtime.getRuntime.availableProcessors}")
    .append(" Total Memory:" + Runtime.getRuntime.totalMemory / 1000000 + "Mb")
    .append(" Max Memory:" + Runtime.getRuntime.maxMemory / 1000000 + "Mb")
    .append(" Free Memory:" + Runtime.getRuntime.freeMemory / 1000000 + "Mb")
    .append(" RAM:" + memorySize / 1000000 + "Mb")
    .append('\n')
    .append("=================================================================================================")

  val greeting = new StringBuilder()
    .append('\n')
    .append("=================================================================================================")
    .append('\n')
    .append(s"★ ★ ★  Environment: ${env} Config: ${configFile.getAbsolutePath}  ★ ★ ★")
    .append('\n')
    .append(s"★ ★ ★  TimeZone: ${TimeZone.getDefault.getID} Started at ${LocalDateTime.now}  ★ ★ ★")
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
}
