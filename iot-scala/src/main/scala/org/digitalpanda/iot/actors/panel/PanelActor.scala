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
      if (!lastPullFinished) log.warning(s"Previous request $requestId not fulfilled")
      lastPullFinished = false
      aggregator ! MeasureQuery(nextMeasureId(), panelController.targetMeasures)

    case MeasureQueryResponse(_, measures) => panelController.setMeasures(measures)

    case PanelClockTick => panelController.clockTick()
  }

  private var lastPullFinished: Boolean = true

  private var requestId: Long = 0
  def nextMeasureId() : Long = { requestId += 1; requestId }

}
