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
containers="auth_registry buildhost deblike_minion rhlike_minion sle_minion opensusessh server controller"
for i in ${containers};do
    sudo -i podman kill ${i}
done

echo "Wait for the containers to stop"
for i in ${containers};do
    sudo -i podman wait ${i}
done

echo "Force remove containers"
containers="auth_registry buildhost deblike_minion rhlike_minion sle_minion opensusessh server controller"
for i in ${containers};do
    sudo -i podman rm ${i}
done

echo "Remove volumes"
sudo -i podman volume rm var-cobbler
sudo -i podman volume rm var-search
sudo -i podman volume rm var-salt
sudo -i podman volume rm var-cache
sudo -i podman volume rm var-spacewalk
sudo -i podman volume rm var-log
sudo -i podman volume rm srv-salt
sudo -i podman volume rm srv-www
sudo -i podman volume rm srv-tftpboot
sudo -i podman volume rm srv-formulametadata
sudo -i podman volume rm srv-pillar
sudo -i podman volume rm srv-susemanager
sudo -i podman volume rm srv-spacewalk
sudo -i podman volume rm root
sudo -i podman volume rm ca-cert
sudo -i podman volume rm run-salt-master
sudo -i podman volume rm etc-tls
sudo -i podman volume rm var-pgsql
sudo -i podman volume rm etc-rhn
sudo -i podman volume rm tls-key
sudo -i podman volume rm etc-apache2
sudo -i podman volume rm etc-systemd-multi
sudo -i podman volume rm etc-systemd-sockets
sudo -i podman volume rm etc-salt
sudo -i podman volume rm etc-tomcat
sudo -i podman volume rm etc-cobbler
sudo -i podman volume rm etc-sysconfig
sudo -i podman volume rm etc-postfix
sudo -i podman volume rm etc-sssd

echo "Remove network"
sudo -i podman network rm network

sleep 10
