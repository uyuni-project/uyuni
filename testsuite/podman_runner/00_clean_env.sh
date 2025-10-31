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
  containers="authregistry.lab noauthregistry.lab buildhost deblike_minion rhlike_minion sle_minion opensusessh server controller uyuni-db"
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
  $PODMAN_CMD volume rm var-cobbler
  $PODMAN_CMD volume rm var-search
  $PODMAN_CMD volume rm var-salt
  $PODMAN_CMD volume rm var-cache
  $PODMAN_CMD volume rm var-spacewalk
  $PODMAN_CMD volume rm var-log
  $PODMAN_CMD volume rm srv-salt
  $PODMAN_CMD volume rm srv-www
  $PODMAN_CMD volume rm srv-tftpboot
  $PODMAN_CMD volume rm srv-formulametadata
  $PODMAN_CMD volume rm srv-pillar
  $PODMAN_CMD volume rm srv-susemanager
  $PODMAN_CMD volume rm srv-spacewalk
  $PODMAN_CMD volume rm root
  $PODMAN_CMD volume rm ca-cert
  $PODMAN_CMD volume rm run-salt-master
  $PODMAN_CMD volume rm etc-tls
  $PODMAN_CMD volume rm var-pgsql
  $PODMAN_CMD volume rm etc-rhn
  $PODMAN_CMD volume rm tls-key
  $PODMAN_CMD volume rm etc-apache2
  $PODMAN_CMD volume rm etc-systemd-multi
  $PODMAN_CMD volume rm etc-systemd-sockets
  $PODMAN_CMD volume rm etc-salt
  $PODMAN_CMD volume rm etc-tomcat
  $PODMAN_CMD volume rm etc-cobbler
  $PODMAN_CMD volume rm etc-sysconfig
  $PODMAN_CMD volume rm etc-postfix
  $PODMAN_CMD volume rm etc-sssd

  echo "Remove network"
  $PODMAN_CMD network rm network

  echo "Remove secrets"
  for secret in $($PODMAN_CMD secret ls --format '{{.Name}}' | grep '^uyuni-'); do
      $PODMAN_CMD secret rm $secret
  done
fi