package org.digitalpanda.iot.actors.panel

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import org.digitalpanda.iot._
import org.digitalpanda.iot.raspberrypi.circuits.panel.PanelController

import scala.concurrent.duration.FiniteDuration

object PanelActor {

  case object MeasurePull
  case object PullNewMeasure
  case object PanelUpdate
  case object PanelClockTick

  def props(panelController: PanelController,
            dataRefreshPeriod: FiniteDuration,
            displayRefreshPeriod: FiniteDuration,
            aggregator: ActorRef) : Props =
    Props(new PanelActor(panelController, dataRefreshPeriod, displayRefreshPeriod, aggregator))
}

class PanelActor(panelController: PanelController,
                 dataRefreshPeriod: FiniteDuration,
                 displayRefreshPeriod: FiniteDuration,
                 aggregator: ActorRef) extends Actor with ActorLogging with Timers {
  import PanelActor._

  timers.startTimerWithFixedDelay(MeasurePull, PullNewMeasure, dataRefreshPeriod)
  timers.startTimerWithFixedDelay(PanelUpdate, PanelClockTick, displayRefreshPeriod)

  override def receive: Receive = {
    case PullNewMeasure =>
      if (lastRequestIdResponded != requestId) log.warning(s"Previous request $requestId not fulfilled")
      aggregator ! MeasureQuery(nextMeasureId(), panelController.targetMeasures)

    case MeasureQueryResponse(requestIdResponse, measures) =>
      panelController.setMeasures(measures)
      lastRequestIdResponded = requestIdResponse

    case PanelClockTick => panelController.clockTick()
  }

  override def postStop() : Unit = {
    log.info(s"Shutdown display panel - begin")
    panelController.shutdown()
    log.info(s"Shutdown display panel - end")
  }

  private var lastRequestIdResponded: Long = 0

  private var requestId: Long = 0
  def nextMeasureId() : Long = { requestId += 1; requestId }

}
