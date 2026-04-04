#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec server bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/rhn/spacewalk-common-channels.ini"
$PODMAN_CMD exec server bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/rhn/spacewalk-common-channels.ini"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_core"
