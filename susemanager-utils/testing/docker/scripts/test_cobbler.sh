#!/bin/sh -e

function configure_cobbler_networking() {
    # Backport of upstream script
    # https://github.com/cobbler/cobbler/blob/main/system-tests/scripts/bootstrap
    server=192.168.1.1
    bridge=pxe
    etc_qemu=$(test -e /etc/qemu-kvm && echo /etc/qemu-kvm || echo /etc/qemu)

    ip link add ${bridge} type bridge
    ip address add ${server}/24 dev ${bridge}
    ip link set up dev ${bridge}

    mkdir -p ${etc_qemu}
    echo allow ${bridge} >>${etc_qemu}/bridge.conf

    sed -i "s/127\.0\.0\.1/${server}/g" /etc/cobbler/settings.yaml
}

function start_httpd() {
    # TODO: This should not be necessary after next Cobbler release
    # See https://github.com/openSUSE/cobbler/pull/97
    /usr/sbin/start_apache2 -D SYSTEMD  -k start || :
}

function configure_cobbler() {
    # Configure and start Cobbler services necessary for testing
    setup-supervisor.sh || :
}

function run_tests() {
    pytest --junitxml=/reports/cobbler.xml
}

configure_cobbler_networking
start_httpd
configure_cobbler
run_tests
