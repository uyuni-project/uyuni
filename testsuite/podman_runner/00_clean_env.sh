#!/bin/bash
set -x

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [ -z "${UYUNI_PROJECT}" ];then
    echo "Set and export UYUNI_PROJECT variable"
    exit 1
fi

if [ -z "${UYUNI_VERSION}" ];then
    echo "Set and export UYUNI_VERSION variable"
    exit 2
fi

echo "Remove temporary directories"
sudo -i rm -rf /tmp/{testing,ssl}

if [[ "$(uname)" == "Darwin" ]]; then
  podman machine stop ||:
  podman machine rm --force ||:
else
  echo "Killing old containers"
  containers="authregistry.lab noauthregistry.lab buildhost deblike_minion rhlike_minion sle_minion server controller uyuni-db"  # opensusessh --- IGNORE ---
  for i in ${containers};do
      $PODMAN_CMD kill ${i}
  done

  echo "Wait for the containers to stop"
  for i in ${containers};do
      $PODMAN_CMD wait ${i}
  done

  echo "Force remove containers"
  for i in ${containers};do
      $PODMAN_CMD rm ${i}
  done

  echo "Remove volumes"
  $PODMAN_CMD volume rm -f var-cobbler
  $PODMAN_CMD volume rm -f var-search
  $PODMAN_CMD volume rm -f var-salt
  $PODMAN_CMD volume rm -f var-cache
  $PODMAN_CMD volume rm -f var-spacewalk
  $PODMAN_CMD volume rm -f var-log
  $PODMAN_CMD volume rm -f srv-salt
  $PODMAN_CMD volume rm -f srv-www
  $PODMAN_CMD volume rm -f srv-tftpboot
  $PODMAN_CMD volume rm -f srv-formulametadata
  $PODMAN_CMD volume rm -f srv-pillar
  $PODMAN_CMD volume rm -f srv-susemanager
  $PODMAN_CMD volume rm -f srv-spacewalk
  $PODMAN_CMD volume rm -f root
  $PODMAN_CMD volume rm -f ca-cert
  $PODMAN_CMD volume rm -f run-salt-master
  $PODMAN_CMD volume rm -f etc-tls
  $PODMAN_CMD volume rm -f var-pgsql
  $PODMAN_CMD volume rm -f etc-rhn
  $PODMAN_CMD volume rm -f tls-key
  $PODMAN_CMD volume rm -f etc-apache2
  $PODMAN_CMD volume rm -f etc-systemd-multi
  $PODMAN_CMD volume rm -f etc-systemd-sockets
  $PODMAN_CMD volume rm -f etc-salt
  $PODMAN_CMD volume rm -f etc-tomcat
  $PODMAN_CMD volume rm -f etc-cobbler
  $PODMAN_CMD volume rm -f etc-sysconfig
  $PODMAN_CMD volume rm -f etc-postfix
  $PODMAN_CMD volume rm -f etc-sssd

  echo "Remove network"
  $PODMAN_CMD network rm -f network

  echo "Remove secrets"
  for secret in $($PODMAN_CMD secret ls --format '{{.Name}}' | grep '^uyuni-'); do
      $PODMAN_CMD secret rm $secret
  done

  echo "Remove custom images"
  $PODMAN_CMD rmi -f "uyuni-server-built:${UYUNI_VERSION}" uyuni-server-built
fi
