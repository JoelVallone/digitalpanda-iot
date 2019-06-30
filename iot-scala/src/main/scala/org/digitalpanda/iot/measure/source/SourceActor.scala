package org.digitalpanda.iot.measure.source

import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import org.digitalpanda.iot.NewMeasure

import scala.concurrent.duration.FiniteDuration


abstract class SourceActor(samplingPeriod: FiniteDuration, measureLocation: String, aggregator: ActorRef) extends Actor with ActorLogging with Timers {

  private case object MeasurePush
  private case object PushNewMeasure

  private var requestId: Long = 0

  timers.startTimerWithFixedDelay(MeasurePush, PushNewMeasure, samplingPeriod)

  override def receive: Receive = {
    case PushNewMeasure => aggregator ! sampleNewMeasure(nextMeasureId())
    case message => log.warning(s"A measure source actor should not receive any external message.\nReceived: $message")
  }

  def nextMeasureId() : Long = {
    requestId += 1
    requestId
  }

  def sampleNewMeasure(requestId: Long): NewMeasure
}
