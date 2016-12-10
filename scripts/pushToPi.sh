#!/usr/bin/env bash
SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
IOT_FOLDER="${SCRIPT_FOLDER}/.."
PI_IP="192.168.0.202"


set -e

trap ctrl_c INT

function ctrl_c() {
    echo "Stopping services"
    pkill cd - || true;
    exit 0;
}
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64";

cd ${IOT_FOLDER};
mvn clean install
scp ${IOT_FOLDER}/target/iot*.jar pi@${PI_IP}:./sense
cd -;
