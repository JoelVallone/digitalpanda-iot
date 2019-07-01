package org.digitalpanda.iot.panel

import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot.raspberrypi.circuits.PanelDisplay
import org.digitalpanda.iot.{Location, Measure, Timestamp}

class PanelController(outdoorMetric : (Location, MeasureType),
                      indoorMetric : (Location, MeasureType),
                      panelDisplay: PanelDisplay) {

  val MetricFreshnessDelayMillis = 30000L
  val targetMeasures = List(outdoorMetric, indoorMetric)

  private var latestMeasures : Map[(Location, MeasureType), (Timestamp, Measure)] = Map()


  def setMeasures(measures: Map[(Location, MeasureType), (Timestamp, Measure)]): Unit =
    latestMeasures = measures

  def clockTick(): Unit = {

    updateWindowInfo()

    panelDisplay.applyStateToDisplay()
  }

  def updateWindowInfo(): Unit = {
    val outdoorMeasure = latestMeasures.get(outdoorMetric)
    val indoorMeasure = latestMeasures.get(indoorMetric)
    val freshnessLowerBoundMillis = System.currentTimeMillis() - MetricFreshnessDelayMillis
    if (
      outdoorMeasure.isDefined && indoorMeasure.isDefined
        && outdoorMeasure.get._1 > freshnessLowerBoundMillis
        && indoorMeasure.get._1 > freshnessLowerBoundMillis
    )
      if (outdoorMeasure.get._2 > indoorMeasure.get._2) {
        panelDisplay.setOpenWindow(false)
      } else
        panelDisplay.setOpenWindow(true)
    else
      panelDisplay.panicWindowState()
  }

}
