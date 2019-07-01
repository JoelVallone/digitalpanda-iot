package org.digitalpanda.iot.raspberrypi.circuits

import com.pi4j.io.gpio.{Pin, RaspiPin}
import org.digitalpanda.iot.raspberrypi.components.Diode

object PanelDisplay {

  def apply(): PanelDisplay =
    new PanelDisplay(RaspiPin.GPIO_21, RaspiPin.GPIO_22)

  def apply(windowRedDiodeId: Pin, windowGreenDiodeId: Pin): PanelDisplay =
    new PanelDisplay(windowRedDiodeId, windowGreenDiodeId)
}

class PanelDisplay (windowRedDiodeId: Pin,
                    windowGreenDiodeId: Pin) {

  private val windowRedDiode = new Diode(windowRedDiodeId, "windowRedDiode")
  private val windowGreenDiode = new Diode(windowGreenDiodeId, "windowGreenDiode")

  def panicWindowState(): Unit = {
    windowGreenDiode.targetDisabled()
    windowRedDiode.targetBlink()
  }

  def setOpenWindow(canOpenWindow: Boolean): Unit = {
    if (canOpenWindow) {
      windowRedDiode.targetDisabled()
      windowGreenDiode.targetEnabled()
    } else {
      windowRedDiode.targetEnabled()
      windowGreenDiode.targetDisabled()
    }
  }

  def applyStateToDisplay(): Unit = {
    windowRedDiode.updateVoltage()
    windowGreenDiode.updateVoltage()
  }

}
