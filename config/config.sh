#!/usr/bin/env bash

SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PI_IP=("192.168.0.211" "192.168.0.212")
PI_HOSTNAME=("raspberrypi-zero-1" "raspberrypi-zero-2")
PI_JAR=("iot-java-0.1.0" "iot-java-0.1.0")
PI_1_IOT_SCRIPT_CONFIG=("jarName,${PI_JAR[0]}")
PI_2_IOT_SCRIPT_CONFIG=("jarName,${PI_JAR[1]}")
