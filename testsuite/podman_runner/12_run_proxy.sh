#!/bin/bash
set -euxo pipefail

create_proxy_configuration() {

  export PROXY_UTILS=$HOME/proxy

  sudo --login podman exec uyuni-server-all-in-one-test bash -c 'cp /root/ssl-build/RHN-ORG-TRUSTED-SSL-CERT /root/ssl-build/uyuni-server-all-in-one-test/server.crt /root/ssl-build/uyuni-server-all-in-one-test/server.key /tmp'

  mkdir --parents \
    $PROXY_UTILS \
    $PROXY_UTILS/proxy-squid-cache \
    $PROXY_UTILS/proxy-rhn-cache \
    $PROXY_UTILS/proxy-tftpboot

  cat <<EOF > $PROXY_UTILS/config.yaml
server: uyuni-server-all-in-one-test
ca_crt: |
$(sudo --login sed 's/^/  /' /tmp/test-all-in-one/RHN-ORG-TRUSTED-SSL-CERT)
proxy_fqdn: proxy-httpd
max_cache_size_mb: 2048
server_version: 5.0.0 Beta1
email: galaxy-noise@suse.de
EOF

  cat <<EOF > $PROXY_UTILS/httpd.yaml
httpd:
  system_id: <?xml version="1.0"?><params><param><value><struct><member><name>username</name><value><string>admin</string></value></member><member><name>os_release</name><value><string>(unknown)</string></value></member><member><name>operating_system</name><value><string>(unknown)</string></value></member><member><name>architecture</name><value><string>x86_64-redhat-linux</string></value></member><member><name>system_id</name><value><string>ID-1000010003</string></value></member><member><name>type</name><value><string>REAL</string></value></member><member><name>fields</name><value><array><data><value><string>system_id</string></value><value><string>os_release</string></value><value><string>operating_system</string></value><value><string>architecture</string></value><value><string>username</string></value><value><string>type</string></value></data></array></value></member><member><name>checksum</name><value><string>1d835605853e545ae07265a1b2317e14ce44162ef70581b0e4f0e044d2f17d25</string></value></member></struct></value></param></params>
  server_crt: |
$(sudo --login sed 's/^/      /' /tmp/test-all-in-one/server.crt)
  server_key: |
$(sudo --login sed 's/^/      /' /tmp/test-all-in-one/server.key)
EOF

  ssh-keygen -t rsa -b 4096 -C "salt@uyuni-server-all-in-one-test" -f /tmp/test-all-in-one/id_openssh_rsa -N ""
  cat <<EOF > $PROXY_UTILS/ssh.yaml
ssh:
  server_ssh_key_pub: |
$(sudo --login sed 's/^/    /' /tmp/test-all-in-one/ssh_host_rsa_key.pub)
  server_ssh_push: |
$(sudo --login sed 's/^/    /' /tmp/test-all-in-one/id_openssh_rsa)
  server_ssh_push_pub: |
$(sudo --login sed 's/^/    /' /tmp/test-all-in-one/id_openssh_rsa.pub)
EOF
}

run_proxy_containers() {
  sudo --login podman pod create \
    --name uyuni-proxy-test \
    --publish 69:69 \
    --publish 80:80 \
    --publish 443:443 \
    --publish 4555:4505 \
    --publish 4556:4506 \
    --publish 8022:22 \
    --publish 8088:8080 \
    --add-host uyuni-server-all-in-one-test:$(sudo --login podman exec uyuni-server-all-in-one-test bash -c 'hostname -I | cut -d" " -f1')

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --tty \
    --pod uyuni-proxy-test \
    --network uyuni-network-1 \
    --cgroupns host \
    --volume $PROXY_UTILS/:/etc/uyuni \
    --volume $PROXY_UTILS/proxy-rhn-cache/:/var/cache/rhn \
    --volume $PROXY_UTILS/proxy-tftpboot/:/srv/tftpboot \
    --name proxy-httpd \
      registry.opensuse.org/uyuni/proxy-httpd:2024.02

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --tty \
    --pod uyuni-proxy-test \
    --network uyuni-network-1 \
    --cgroupns host \
    --volume $PROXY_UTILS/:/etc/uyuni \
    --volume /tmp/test-all-in-one:/tmp \
    --name proxy-ssh \
      registry.opensuse.org/uyuni/proxy-ssh:2024.02

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --tty \
    --pod uyuni-proxy-test \
    --network uyuni-network-1 \
    --cgroupns host \
    --volume $PROXY_UTILS/:/etc/uyuni \
    --name proxy-salt-broker \
      registry.opensuse.org/uyuni/proxy-salt-broker:2024.02

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --tty \
    --pod uyuni-proxy-test \
    --network uyuni-network-1 \
    --cgroupns host \
    --volume $PROXY_UTILS/:/etc/uyuni \
    --volume $PROXY_UTILS/proxy-squid-cache/:/var/cache/squid \
    --name proxy-squid \
      registry.opensuse.org/uyuni/proxy-squid:2024.02

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --tty \
    --pod uyuni-proxy-test \
    --network uyuni-network-1 \
    --cgroupns host \
    --volume $PROXY_UTILS/:/etc/uyuni \
    --volume $PROXY_UTILS/proxy-tftpboot/:/srv/tftpboot \
    --name proxy-tftpd \
      registry.opensuse.org/uyuni/proxy-tftpd:2024.02
}

log_status() {
  cat <<- EOF | awk 'NR==1 && match($0, /^ +/){n=RLENGTH} {print substr($0, n+1)}' > /tmp/test-all-in-one/podman-proxy.log 2>&1
    uyuni-proxy-test-status: $(sudo --login podman pod inspect --format '{{.State}}' uyuni-proxy-test)
    proxy-http-status: $(sudo --login podman container inspect --format '{{.State.Status}}' proxy-httpd)
    proxy-ssh-status: $(sudo --login podman container inspect --format '{{.State.Status}}' proxy-ssh)
    proxy-squid-status: $(sudo --login podman container inspect --format '{{.State.Status}}' proxy-squid)
    proxy-salt-broker-status: $(sudo --login podman container inspect --format '{{.State.Status}}' proxy-salt-broker)
    proxy-tftpd-status: $(sudo --login podman container inspect --format '{{.State.Status}}' proxy-tftpd)
    uyuni-proxy-test-containers: $(sudo --login podman pod inspect --format '{{.NumContainers}}' uyuni-proxy-test)
EOF
}


cleanup() {
  sudo --login \
    rm \
      /tmp/test-all-in-one/id_openssh_rsa \
      /tmp/test-all-in-one/id_openssh_rsa.pub \
      /tmp/test-all-in-one/RHN-ORG-TRUSTED-SSL-CERT \
      /tmp/test-all-in-one/server.crt \
      /tmp/test-all-in-one/server.key
}

create_proxy_configuration
run_proxy_containers
log_status
cleanup

sudo -i podman exec controller-test bash -c "cat /root/.ssh/config && cat /root/.ssh/authorized_keys"
sudo -i podman ps
sudo -i podman pod ls

cat /tmp/test-all-in-one/podman-proxy.log
# cat /tmp/test-all-in-one/podman-proxy-pod-state.log
# cat /tmp/test-all-in-one/podman-proxy-httpd.log
# cat /tmp/test-all-in-one/podman-proxy-ssh.log
# cat /tmp/test-all-in-one/podman-proxy-salt-broker.log
# cat /tmp/test-all-in-one/podman-proxy-squid.log
# cat /tmp/test-all-in-one/podman-proxy-tftpd.log
# cat /tmp/test-all-in-one/podman-proxy-pod-containers.log