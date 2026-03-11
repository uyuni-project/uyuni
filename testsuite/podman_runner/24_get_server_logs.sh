#!/bin/bash
set -x

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [ $# -ne 1 ];
then
	echo "Usage: ${0} server_id"
	echo "server_id is used for creating a unique folder"
	exit 1
fi

server_id=${1}
HOST_LOG_DIR="testing/server-logs/${server_id}"
SUT_LOG_DIR="server-logs/${server_id}"

rm -rfv "/tmp/${HOST_LOG_DIR}"
mkdir -p "/tmp/${HOST_LOG_DIR}" && chmod -R 755 "/tmp/${HOST_LOG_DIR}"
# shellcheck disable=SC2024
sudo -i journalctl > "/tmp/${HOST_LOG_DIR}/journalctl.log" && chmod 644 "/tmp/${HOST_LOG_DIR}/journalctl.log"
# prevent supportconfig exit code 1 on missing logger bsc#1245667
$PODMAN_CMD exec server bash -c "ln -s /usr/bin/echo /usr/bin/logger"
$PODMAN_CMD exec server bash -c "supportconfig -R /tmp/${SUT_LOG_DIR}; chmod 644 /tmp/${SUT_LOG_DIR}/*.txz*"

