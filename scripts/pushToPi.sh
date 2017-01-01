#!/usr/bin/env bash
SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
IOT_FOLDER="${SCRIPT_FOLDER}/.."
PI_IP="192.168.0.211"


set -e

trap ctrl_c INT

function ctrl_c() {
    ssh pi@${PI_IP} "./iot.sh stop" < /dev/null || true
    cd - || true;
    exit 0;
}
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64";

cd ${IOT_FOLDER};
mvn clean install
ssh pi@${PI_IP} 'mkdir -p ~/sense' < /dev/null
scp ${IOT_FOLDER}/target/iot*.jar pi@${PI_IP}:./sense
scp ${IOT_FOLDER}/scripts/iot.sh pi@${PI_IP}:.
ssh pi@${PI_IP} "chmod 755 ./iot.sh;" < /dev/null
#ssh pi@${PI_IP} "ln -s ~/iot.sh /etc/init.d/iot && update-rc.d iot defaults" < /dev/null

if [ $1 == "run" ]; then
    ssh pi@${PI_IP} "./iot.sh start" < /dev/null
fi
cd -;
