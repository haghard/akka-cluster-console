package console

import java.io.File
import java.util.TimeZone
import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializerSettings
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection._

object Application extends App with AppSupport {
  //-Duser.timezone=UTC
  //TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  val opts: Map[String, String] = argsToOpts(args.toList)
  applySystemProperties(opts)

  val tcpPort  = System.getProperty("akka.remote.netty.tcp.port")
  val httpPort = System.getProperty("akka.http.port")
  val hostName = System.getProperty("HOSTNAME")
  val confPath = System.getProperty("CONFIG")

  val httpConf =
    s"""
       |akka.remote.netty.tcp.port=%port%
       |akka.http.port=%httpP%
       |akka.remote.netty.tcp.hostname=%hostName%
       |akka.http.interface=%interface%
       |
       |akka.http.session {
       |  header {
       |    send-to-client-name = ${shared.Headers.fromServer}
       |    get-from-client-name = ${shared.Headers.fromClient}
       |  }
       | }
       |
    """.stripMargin

  val httpConf1 = httpConf
    .replaceAll("%port%", tcpPort)
    .replaceAll("%httpP%", httpPort)
    .replaceAll("%hostName%", hostName)
    .replaceAll("%interface%", hostName)

  val confDir = new File(confPath)
  //for re~start
  //for alias

  val env        = Option(System.getProperty("ENV")).getOrElse(throw new Exception("ENV is expected"))
  val configFile = new File(s"${new File(confPath).getAbsolutePath}/" + env + ".conf")
  //println("-----------------" + configFile.getAbsoluteFile)

  val config: Config =
    ConfigFactory
      .parseString(httpConf1)
      .withFallback(ConfigFactory.parseFile(configFile).resolve())
      .withFallback(ConfigFactory.load()) //for read seeds from env vars

  implicit val coreSystem: ActorSystem = ActorSystem("cluster-console", config)
  implicit val mat = akka.stream.ActorMaterializer(
    ActorMaterializerSettings
      .create(coreSystem)
      .withDispatcher(Bootstrap.HttpDispatcher)
  )(coreSystem)

  /*coreSystem.actorOf(
      Bootstrap.prop(httpPort.toInt,
        config.getString("akka.http.interface"), config.getString("akka.http.ssl.file"),
        config.getString("akka.http.ssl.keypass"), config.getString("akka.http.ssl.storepass")
      ), "bootstrap")*/

  Bootstrap(config.getString("akka.http.interface"), httpPort.toInt)

  val tz = TimeZone.getDefault.getID
  val greeting = new StringBuilder()
    .append('\n')
    .append("=================================================================================================")
    .append('\n')
    .append(s"★ ★ ★ Akka cluster port: ${config.getInt("akka.remote.netty.tcp.port")} ★ ★ ★")
    .append('\n')
    .append(s"★ ★ ★ Akka seeds: ${config.getStringList("akka.cluster.seed-nodes")} ★ ★ ★")
    .append('\n')
    .append(
      s"★ ★ ★ Environment: ${env} Config: ${configFile.getAbsolutePath} TimeZone: $tz Started at ${LocalDateTime.now} ★ ★ ★"
    )
    .append('\n')
    .append(s"★ ★ ★ Cassandra entry points: ${config.getString("cassandra.hosts")}  ★ ★ ★")
    .append('\n')
    .append(s"★ ★ ★ Server online at http://${config.getString("akka.http.interface")}:${httpPort} ★ ★ ★ ")
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

  coreSystem.log.info(greeting.toString)
}
