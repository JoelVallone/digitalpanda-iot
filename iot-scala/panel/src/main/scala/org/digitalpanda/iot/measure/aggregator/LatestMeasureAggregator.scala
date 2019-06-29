package org.digitalpanda.iot.measure.aggregator

import akka.actor.{Actor, ActorLogging}
import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot._

class LatestMeasureAggregator extends Actor with ActorLogging {

  private var measureMap : Map[(Location, MeasureType), (Timestamp, Measure)] = Map()

  override def receive: Receive = {
    case NewMeasure(_, location, measureType, timestamp,  value) =>
      measureMap = measureMap + ((location, measureType) -> (timestamp, value))

    case MeasureQuery(requestId, location, measureType) =>
      sender() ! lookupMeasure(requestId, location, measureType)
  }

  private def lookupMeasure(requestId : Long, location: Location, measureType: MeasureType) : MeasureQueryResponse =
    measureMap
      .get(location, measureType)
      .map { case (timestamp, value) => MeasureQueryResponse(requestId, location, measureType, Some(timestamp), Some(value)) }
      .getOrElse(MeasureQueryResponse(requestId, location, measureType, None, None))
}
