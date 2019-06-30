#!/usr/bin/env bash

#set -x

################################################################################
# DESCRIPTION: Build, deploy and run the iot code on a list of nodes
################################################################################

################################################################################
# VARIABLES AND CONSTANTS  #####################################################
################################################################################
set -e
SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
IOT_FOLDER="${SCRIPT_FOLDER}/../iot-java"
source ${IOT_FOLDER}/config/config.sh
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64";

################################################################################
# HELPER FUNCTIONS #############################################################
################################################################################
function clean_exit() {
    notify "STOPPING CODE ON ${1}"
    ssh pi@${1} "[ -f iot.sh ] && ./iot.sh stop" < /dev/null || true
    cd - &> "/dev/null" || true;
    exit 0;
}

function showUsage(){
    echo -e "USAGE: $(basename $0) :\n\
\t[-d|--deploy{comma separated list of node-ids in (1..${#PI_IP[@]}) OR \"all\"}]\n\
\t[-r|--run {node-id on which to run the code}]"
    exit 1;
}

function notify(){
    echo -e "\n==> $1 <=="
}

function printConfig(){
    echo "Sensor Network Configuration:"
    for i in $(seq 0 $((${#PI_IP[@]}-1))); do
        echo "node-id=$(($i + 1)) : hostname=${PI_HOSTNAME[$i]}, ip=${PI_IP[$i]}"
        cat "${SCRIPT_FOLDER}/${PI_HOSTNAME[$i]}.properties"
        echo -e "\n"
    done
}

function isNodeId(){
    [[ ${1} =~ ^[0-9]+$ ]] && [ $1 -ge 1 -a $1 -le ${#PI_IP[@]} ]
}

################################################################################
# PARSE USER INPUT #############################################################
################################################################################
printConfig
DEPLOY_TARGETS=""
RUN_TARGET="none"
while [ $# -gt 0 ]; do
    case "$1" in
      --deploy|-d)
        shift
        if [ $# > 0 ]; then
            if [ $1 == "all" ]; then
                LAST_ARRAY_ID=$((${#PI_IP[@]}-1))
                DEPLOY_TARGETS=($(seq 1 ${LAST_ARRAY_ID}))
            else
                DEPLOY_TARGETS=(${1//,/\ })
            fi
         else
            showUsage
         fi
      ;;
      --run|-r)
        shift
        [ $# > 0 ] && RUN_TARGET=$1 || showUsage
      ;;
      *)
        showUsage
    esac
    shift
done

################################################################################
# MAIN PROGRAM #################################################################
################################################################################
notify "COMPILING CODE"
cd ${IOT_FOLDER};
mvn clean install

for i in ${DEPLOY_TARGETS[@]}; do
    ! isNodeId $i && continue
    IP=${PI_IP[$((${i} - 1))]};  HOSTNAME=${PI_HOSTNAME[$((${i} - 1))]}
    notify "DEPLOYING CODE ON : $HOSTNAME,$IP"
    ssh pi@${IP} "[ -e ./iot.sh ] && sudo ./iot.sh stop || true" < /dev/null
    ssh pi@${IP} '[ -e ./sense ] && rm -f ./sense/*' || true < /dev/null
    ssh pi@${IP} 'mkdir -p ~/sense' < /dev/null
    scp ${IOT_FOLDER}/target/iot-java-0.1.0.jar pi@${IP}:./sense
    scp ${IOT_FOLDER}/config/${HOSTNAME}.properties pi@${IP}:./sense/configuration.properties
    scp ${IOT_FOLDER}/scripts/iot.sh pi@${IP}:.
    ssh pi@${PI_IP} "sudo ln -fs ~/iot.sh /etc/init.d/iot && sudo update-rc.d iot defaults" < /dev/null
    ssh pi@${IP} "chmod 755 ./iot.sh;sudo systemctl daemon-reload" < /dev/null
done

if [ ${RUN_TARGET} != "none" ] && isNodeId ${RUN_TARGET}; then
    IP=${PI_IP[$((${RUN_TARGET}-1))]}
    trap "clean_exit ${IP}" INT
    notify "STARTING CODE ON : ${IP}"
    ssh pi@${IP} "sudo service iot start" < "/dev/null"
fi
cd - &> "/dev/null";
