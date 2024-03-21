#!/bin/bash
set -euxo pipefail

get_server_certificates() {
  sudo -i podman exec uyuni-server-all-in-one-test bash -c 'cp /root/ssl-build/RHN-ORG-TRUSTED-SSL-CERT /tmp'
  sudo -i podman exec uyuni-server-all-in-one-test bash -c 'cp /root/ssl-build/uyuni-server-all-in-one-test/server.crt /tmp'
  sudo -i podman exec uyuni-server-all-in-one-test bash -c 'cp /root/ssl-build/uyuni-server-all-in-one-test/server.key /tmp'
}

create_proxy_configuration() {
  cat <<EOF > $HOME/config.yaml
server: uyuni-server-all-in-one-test
ca_crt: |
$(sudo --login sed 's/^/  /' /tmp/test-all-in-one/RHN-ORG-TRUSTED-SSL-CERT)
proxy_fqdn: proxy-httpd
max_cache_size_mb: 2048
server_version: 5.0.0 Beta1
email: galaxy-noise@suse.de
EOF

  cat <<EOF > $HOME/httpd.yaml
httpd:
  system_id: <?xml version="1.0"?><params><param><value><struct><member><name>username</name><value><string>admin</string></value></member><member><name>os_release</name><value><string>(unknown)</string></value></member><member><name>operating_system</name><value><string>(unknown)</string></value></member><member><name>architecture</name><value><string>x86_64-redhat-linux</string></value></member><member><name>system_id</name><value><string>ID-1000010003</string></value></member><member><name>type</name><value><string>REAL</string></value></member><member><name>fields</name><value><array><data><value><string>system_id</string></value><value><string>os_release</string></value><value><string>operating_system</string></value><value><string>architecture</string></value><value><string>username</string></value><value><string>type</string></value></data></array></value></member><member><name>checksum</name><value><string>1d835605853e545ae07265a1b2317e14ce44162ef70581b0e4f0e044d2f17d25</string></value></member></struct></value></param></params>
  server_crt: |
$(sudo --login sed 's/^/      /' /tmp/test-all-in-one/server.crt)
  server_key: |
$(sudo --login sed 's/^/      /' /tmp/test-all-in-one/server.key)
EOF

  ssh-keygen -t rsa -b 4096 -C "salt@uyuni-server-all-in-one-test" -f /tmp/test-all-in-one/id_openssh_rsa -N ""
  cat <<EOF > $HOME/ssh.yaml
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
  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --network uyuni-network-1 \
    --volume $HOME/:/etc/uyuni \
    --name proxy-httpd \
    --hostname proxy-httpd \
    --publish 80:80 \
    --publish 443:443 \
      registry.suse.com/suse/manager/4.3/proxy-httpd

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --network uyuni-network-1 \
    --volume $HOME/:/etc/uyuni \
    --name proxy-salt-broker \
    --hostname proxy-salt-broker \
    --publish 4555:4505 \
    --publish 4556:4506 \
      registry.suse.com/suse/manager/4.3/proxy-salt-broker

  sudo --login podman run \
    --privileged \
    --user root \
    --rm \
    --detach \
    --network uyuni-network-1 \
    --volume $HOME/:/etc/uyuni \
    --name proxy-squid \
    --hostname proxy-squid \
    --publish 8088:8088 \
      registry.suse.com/suse/manager/4.3/proxy-squid

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --network uyuni-network-1 \
    --volume $HOME/:/etc/uyuni \
    --volume /tmp/test-all-in-one:/tmp \
    --name proxy-ssh \
    --hostname proxy-ssh \
    --publish 8022:22 \
      registry.suse.com/suse/manager/4.3/proxy-ssh

  sudo --login podman run \
    --privileged \
    --rm \
    --detach \
    --network uyuni-network-1 \
    --volume $HOME/:/etc/uyuni \
    --name proxy-tftpd \
    --hostname proxy-tftpd \
    --publish 69:69 \
      registry.suse.com/suse/manager/4.3/proxy-tftpd
}

add_ssh_configuration () {
  sudo --login podman exec proxy-ssh bash -c "mv /tmp/ssh_host_rsa_key.pub /root/.ssh/authorized_keys"
}

cleanup () {
  sudo --login rm /tmp/test-all-in-one/id_openssh_rsa /tmp/test-all-in-one/id_openssh_rsa.pub /tmp/test-all-in-one/RHN-ORG-TRUSTED-SSL-CERT /tmp/test-all-in-one/server.crt /tmp/test-all-in-one/server.key
}

get_server_certificates
create_proxy_configuration
run_proxy_containers
add_ssh_configuration
cleanup

sudo -i podman ps
