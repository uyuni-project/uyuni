#!/bin/bash
set -xe
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /testsuite/podman_runner/debug_logging.properties /etc/tomcat/logging.properties"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /java && ant -f manager-build.xml ivy refresh-branding-jar deploy-local"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "set -xe;cd /web/html/src;[ -d dist ] || mkdir dist;yarn install --force --ignore-optional --production=true --frozen-lockfile;yarn autoclean --force;yarn build:novalidate; rsync -a dist/ /srv/www/htdocs/"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "rctomcat restart"

# mgr-push
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /client/tools/mgr-push/*.py /usr/lib/python3.6/site-packages/rhnpush/"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /client/tools/mgr-push/rhnpushrc /etc/sysconfig/rhn/rhnpushrc"

