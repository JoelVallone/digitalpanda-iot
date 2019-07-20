package org.digitalpanda.iot.app

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.pi4j.io.gpio.RaspiPin
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.digitalpanda.iot.MeasureType
import org.digitalpanda.iot.actors.panel.PanelActor
import org.digitalpanda.iot.measure.aggregator.LatestMeasureAggregator
import org.digitalpanda.iot.measure.source.CassandraDbSource
import org.digitalpanda.iot.raspberrypi.circuits.panel.{PanelController, PanelDisplay}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PanelApp extends App {
  
  private val logger = Logger(classOf[PanelController])
  val conf = ConfigFactory.load()

  logger.info(s"PanelApp config: $conf")

  logger.info("> Instantiate panelApp actors")

  val system: ActorSystem = ActorSystem("panelApp")

  logger.info(">> Measure aggregator")
  val measureAggregator  = system.actorOf(LatestMeasureAggregator.props(), "latestMeasuresAggregator")

  logger.info(">> Measure source from database")
  system.actorOf(
    CassandraDbSource.props(
      conf.getString("cassandra.contactpoint"), conf.getInt("cassandra.port"),
      Duration.create(conf.getLong("panel.data.pull-period-millis"), TimeUnit.MILLISECONDS),
      measureAggregator
    ),
    "dbMeasureSource")

  logger.info(">> Panel")
  system.actorOf(
    PanelActor.props(
      new PanelController(
        (conf.getString("panel.data.window.out.name"), MeasureType.TEMPERATURE),
        (conf.getString("panel.data.window.out.name"), MeasureType.TEMPERATURE),
        PanelDisplay(
          windowRedDiodeId = RaspiPin.GPIO_29,
          windowGreenDiodeId = RaspiPin.GPIO_28)
      ),
      dataRefreshPeriod = Duration.create(1000, TimeUnit.MILLISECONDS),
      displayRefreshPeriod = Duration.create(conf.getLong("panel.display.refresh-period-millis"), TimeUnit.MILLISECONDS),
      measureAggregator
    ),
    "panelActor")

  logger.info("> Register shutdown hook")
  sys.addShutdownHook( () => {
    logger.info("Shutting down panelApp actor system")
    //TODO: Handle clean shutdown
    Await.result(system.terminate(), Duration(10, TimeUnit.SECONDS))
    logger.info("panelApp end")
  })

  logger.info("> Wait for OS signals")
  while (true) Thread.sleep(1000L)

}
