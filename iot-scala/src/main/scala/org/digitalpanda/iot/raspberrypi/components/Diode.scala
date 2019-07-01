package org.digitalpanda.iot.raspberrypi.components

import java.util.concurrent.TimeUnit

import com.pi4j.io.gpio.{GpioFactory, Pin, PinState}


class Diode(val gpioId: Pin, val pinName: String) {

  private val gpioPin = GpioFactory.getInstance.provisionDigitalOutputPin(gpioId, pinName, PinState.LOW)

  private var _targetBlink = false
  private var _targetEnabled = false

  def targetBlink(): Unit = {
    _targetBlink = true
    _targetEnabled = false
  }

  def targetEnabled(): Unit = {
    _targetBlink = false
    _targetEnabled = true
  }

  def targetDisabled(): Unit = {
    _targetBlink = false
    _targetEnabled = false
  }

  def updateVoltage(): Unit = {
    if (_targetBlink)
      gpioPin.blink(250, TimeUnit.MILLISECONDS)
    else {
      gpioPin.blink(0, TimeUnit.MILLISECONDS)
      gpioPin.setState(_targetEnabled)
    }
  }
}
