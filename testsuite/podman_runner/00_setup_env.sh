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
containers="deblike_minion rhlike_minion sle_minion opensusessh uyuni-server-all-in-one-test controller-test"
for i in ${containers};do
    ${CR} kill ${i}
done

echo "Remove network"
${CR} network rm uyuni-network-1

echo "Wait for the containers to stop"
for i in ${containers};do
    ${CR} wait ${i}
done

sleep 10
