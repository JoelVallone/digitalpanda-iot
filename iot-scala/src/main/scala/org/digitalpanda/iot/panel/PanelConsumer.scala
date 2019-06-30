package org.digitalpanda.iot.panel

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import org.digitalpanda.iot._
import org.digitalpanda.iot.MeasureType.MeasureType

import scala.concurrent.duration.FiniteDuration

object PanelConsumer {

  def props(outdoorMetric : (Location, MeasureType),
            indoorMetric : (Location, MeasureType),
            samplingPeriod: FiniteDuration,
            aggregator: ActorRef) : Props =
    Props(new PanelConsumer(outdoorMetric, indoorMetric, samplingPeriod, aggregator))
}

class PanelConsumer( outdoorMetric : (Location, MeasureType),
                     indoorMetric : (Location, MeasureType),
                     samplingPeriod: FiniteDuration,
                     aggregator: ActorRef) extends Actor with ActorLogging with Timers {

  timers.startTimerWithFixedDelay(MeasurePull, PullNewMeasure, samplingPeriod)

  override def receive: Receive = {
    case PullNewMeasure =>
      if (!lastPullFinished) log.warning(s"Previous request $requestId not fulfilled")
      lastPullFinished = false
      aggregator ! MeasureQuery(nextMeasureId(), List(outdoorMetric, indoorMetric))

    case MeasureQueryResponse(_, measures) =>
      updateDisplayPanel(measures)
  }


  private case object MeasurePull
  private case object PullNewMeasure

  private var lastPullFinished: Boolean = true

  private var requestId: Long = 0
  def nextMeasureId() : Long = { requestId += 1; requestId }

  def updateDisplayPanel(measures: Map[(Location, MeasureType), (Timestamp, Measure)]): Unit = ???

}
