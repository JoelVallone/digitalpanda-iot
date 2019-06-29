package org.digitalpanda.iot.measure.source

import akka.actor.{ActorRef, Props}
import org.digitalpanda.iot.{Location, NewMeasure}

import scala.concurrent.duration.FiniteDuration

object MeteoSwissSource {
  def props(samplingPeriod: FiniteDuration, location: String, aggregator: ActorRef) : Props =
    Props(new MeteoSwissSource(samplingPeriod, location, aggregator))
}

class MeteoSwissSource(samplingPeriod: FiniteDuration, measureLocation : Location, aggregator: ActorRef)
  extends SourceActor(samplingPeriod, measureLocation, aggregator) {

  override def sampleNewMeasure(requestId: Long): NewMeasure = ???
}
