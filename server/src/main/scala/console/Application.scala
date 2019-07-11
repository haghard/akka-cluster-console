package console

import java.io.File
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
  val httpPort = System.getProperty("akka.http.port")

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

  Bootstrap(hostname, httpPort.toInt, config.getString("psw"))

  val tz = TimeZone.getDefault.getID
  val greeting = new StringBuilder()
    .append('\n')
    .append("=================================================================================================")
    .append('\n')
    .append(
      s"★ ★ ★ Environment: ${env} Config: ${configFile.getAbsolutePath} TimeZone: $tz Started at ${LocalDateTime.now} ★ ★ ★"
    )
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
}
