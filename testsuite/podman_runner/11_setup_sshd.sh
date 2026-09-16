#!/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec server bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec server bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec controller bash --login -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec buildhost bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec buildhost bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
