package org.digitalpanda.iot.panel

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import org.digitalpanda.iot.MeasureType
import org.digitalpanda.iot.measure.aggregator.LatestMeasureAggregator
import org.digitalpanda.iot.measure.source.CassandraDbSource

import scala.concurrent.duration.Duration

object PanelApp extends App {

  println("Instantiate panelApp actors")

  val system: ActorSystem = ActorSystem("panelApp")
  val measureAggregator  = system.actorOf(LatestMeasureAggregator.props(), "latestMeasuresAggregator")
  val dbMeasureSource = system.actorOf(
    CassandraDbSource.props(
      "192.168.0.102", 9042,
      Duration.create(1000, TimeUnit.MILLISECONDS),
      measureAggregator
    ),
    "dbMeasureSource")
  val panel = system.actorOf(
    PanelConsumer.props(
      ("server-room", MeasureType.TEMPERATURE),
      ("outdoor", MeasureType.TEMPERATURE),
      Duration.create(1000, TimeUnit.MILLISECONDS),
      measureAggregator
  ),
    "panel")

  println("Wait for OS signals")
  while (true) Thread.sleep(1000L)
}
