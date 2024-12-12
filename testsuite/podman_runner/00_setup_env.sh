#!/bin/bash
set -x

if [ -z "${UYUNI_PROJECT}" ];then
    echo "Set and export UYUNI_PROJECT variable"
    exit 1
fi

if [ -z "${UYUNI_VERSION}" ];then
    echo "Set and export UYUNI_VERSION variable"
    exit 2
fi

echo "Killing old containers"
containers="deblike_minion rhlike_minion sle_minion opensusessh server controller"
for i in ${containers};do
    sudo -i podman kill ${i}
done

echo "Wait for the containers to stop"
for i in ${containers};do
    sudo -i podman wait ${i}
done

echo "Force remove containers"
containers="deblike_minion rhlike_minion sle_minion opensusessh server controller"
for i in ${containers};do
    sudo -i podman rm ${i}
done

echo "Remove network"
sudo -i podman network rm network

sleep 10
