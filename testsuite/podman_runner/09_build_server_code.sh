#!/bin/bash
set -xe
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /testsuite/podman_runner/debug_logging.properties /etc/tomcat/logging.properties"

# Create missing directories that will be created by the new RPM https://github.com/uyuni-project/uyuni/pull/7651
sudo -i podman exec uyuni-server-all-in-one-test bash -c "[ -d /usr/share/susemanager/www ] || mkdir -p /usr/share/susemanager/www"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "[ -d /usr/share/susemanager/www/htdocs ] || mkdir -p /usr/share/susemanager/www/htdocs"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "[ -d /usr/share/susemanager/www/tomcat/webapps ] || mkdir -p /usr/share/susemanager/www/tomcat/webapps"

# WORKAROUND: If ivy build fails, try again, because 50% of times fails when
# downloading the new jar files from download.opensuse.org.
# 
# build.opensuse.org publishes our packages into download.opensuse.org. However,
# this is not a "static" directory. If packages get rebuild, old packages are
# removed, new packages are published and new metadata is created. Then, the
# whole thing is updated in the download.opensuse.org mirrors all around the
# world.
#
# Packages from devel repos can get rebuild any time, i.e. when their
# dependencies are updates (i.e. java).
# 
# Thus, the repos from devel projects that are published into
# download.opensuse.org, are not stable, their metadata can get rebuilt at any
# time, packages are removed, etc. and plus, this takes time to be propagated,
# so mirrors are very often outdated, have outdated metadata or outdated
# packages, or both.
#
# So, we can not consider download.opensuse.org reliable for devel projects.
# While we do not have a mirror for the jar files, the easiest workaround is
# to try again and hope it succeeds.

sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /java && ant -f manager-build.xml ivy || ant -f manager-build.xml ivy || ant -f manager-build.xml ivy"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /java && ant -f manager-build.xml refresh-branding-jar deploy-local"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /java && ant -f manager-build.xml apidoc-jsp"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "mkdir /usr/share/susemanager/www/tomcat/webapps/rhn/apidoc/ && rsync -av /java/build/reports/apidocs/jsp/ /usr/share/susemanager/www/tomcat/webapps/rhn/apidoc/"

sudo -i podman exec uyuni-server-all-in-one-test bash -c "set -xe;cd /web/html/src;[ -d dist ] || mkdir dist;yarn install --force --ignore-optional --production=true --frozen-lockfile;yarn autoclean --force;yarn build:novalidate; rsync -a dist/ /usr/share/susemanager/www/htdocs/"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "rctomcat restart"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "rctaskomatic restart"

# mgr-push
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /client/tools/mgr-push/*.py /usr/lib/python3.6/site-packages/rhnpush/"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cp /client/tools/mgr-push/rhnpushrc /etc/sysconfig/rhn/rhnpushrc"

sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /susemanager-utils/susemanager-sls/; cp -R modules/* /usr/share/susemanager/modules; cp -R salt/* /usr/share/susemanager/salt; cp -R salt-ssh/* /usr/share/susemanager/salt-ssh"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "cd /susemanager/; cp src/mgr-salt-ssh /usr/bin/; chmod a+x /usr/bin/mgr-salt-ssh"
