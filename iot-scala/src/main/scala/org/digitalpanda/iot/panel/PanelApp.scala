package org.digitalpanda.iot.panel

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import org.digitalpanda.iot.MeasureType
import org.digitalpanda.iot.measure.aggregator.LatestMeasureAggregator
import org.digitalpanda.iot.measure.source.CassandraDbSource
import org.digitalpanda.iot.raspberrypi.circuits.panel.{PanelController, PanelDisplay}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PanelApp extends App {

  println("> Instantiate panelApp actors")

  val system: ActorSystem = ActorSystem("panelApp")

  println(">> Measure aggregator")
  val measureAggregator  = system.actorOf(LatestMeasureAggregator.props(), "latestMeasuresAggregator")

  println(">> Measure source from database")
  system.actorOf(
    CassandraDbSource.props(
      "192.168.0.102", 9042,
      Duration.create(1000, TimeUnit.MILLISECONDS),
      measureAggregator
    ),
    "dbMeasureSource")

  println(">> Panel")
  system.actorOf(
    PanelActor.props(
      new PanelController(
        ("outdoor", MeasureType.TEMPERATURE),
        ("server-room", MeasureType.TEMPERATURE),
        PanelDisplay()
      ),
      Duration.create(1000, TimeUnit.MILLISECONDS),
      Duration.create(250, TimeUnit.MILLISECONDS),
      measureAggregator
    ),
    "panelActor")

  println("> Register shutdown hook")
  sys.addShutdownHook( () => {
    println("Shutting down panelApp actor system")
    Await.result(system.terminate(), Duration(10, TimeUnit.SECONDS))
    println("panelApp end")
  })

  println("> Wait for OS signals")
  while (true) Thread.sleep(1000L)

}
