#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SSH_INIT_CLIENTS_CMD="cd /testsuite && rake cucumber:github_validation_init_clients_ssh_minion"

if ! $PODMAN_CMD exec controller bash --login -c "$SSH_INIT_CLIENTS_CMD"; then
  echo "SSH minion bootstrap failed, restarting opensusessh container and retrying..."
  $PODMAN_CMD restart opensusessh
  $PODMAN_CMD exec opensusessh bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
  $PODMAN_CMD exec opensusessh bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
  $PODMAN_CMD exec opensusessh bash -c "echo 'root:linux' | chpasswd && echo 123456789 > /etc/machine-id"
  $PODMAN_CMD exec opensusessh bash -c "echo -e 'UseDNS no\nPermitRootLogin yes\nUsePAM yes\nSubsystem sftp /usr/libexec/ssh/sftp-server' >> /etc/ssh/sshd_config && ssh-keygen -A && /usr/sbin/sshd -e"
  $PODMAN_CMD exec opensusessh bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
  $PODMAN_CMD exec opensusessh bash -c "rm -f /var/run/dbus/system_bus_socket"
  $PODMAN_CMD exec opensusessh bash -c "zypper rs container-suseconnect-zypp 2>/dev/null; rm -f /usr/lib/zypp/plugins/services/container-suseconnect-zypp 2>/dev/null"
  SERVER_IP=$($PODMAN_CMD inspect -f '{{.NetworkSettings.Networks.network.IPAddress}}' server)
  OPENSUSESSH_IP=$($PODMAN_CMD inspect -f '{{.NetworkSettings.Networks.network.IPAddress}}' opensusessh)
  $PODMAN_CMD exec server bash -c "sed -i '/opensusessh/d' /etc/hosts; echo '$OPENSUSESSH_IP opensusessh' >> /etc/hosts"
  $PODMAN_CMD exec opensusessh bash -c "sed -i '/server/d' /etc/hosts; echo '$SERVER_IP server' >> /etc/hosts"
  $PODMAN_CMD exec controller bash --login -c "$SSH_INIT_CLIENTS_CMD"
fi

echo "Running remaining init client tests..."
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_others"
