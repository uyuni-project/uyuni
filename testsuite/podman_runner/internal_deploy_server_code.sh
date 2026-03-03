#!/bin/bash
set -xe

# Set debug logging
cp /testsuite/podman_runner/debug_logging.properties /etc/tomcat/logging.properties

# Create missing directories
mkdir -p /usr/share/susemanager/www
mkdir -p /usr/share/susemanager/www/htdocs
mkdir -p /usr/share/susemanager/www/tomcat/webapps

# WORKAROUND: If ivy build fails, try again
set +e
MAX_WAIT=1200
START_TIME=$(date +%s)
cd /java
while ! ant -f manager-build.xml ivy; do
    CURRENT_TIME=$(date +%s)
    ELAPSED_SECONDS=$((CURRENT_TIME - START_TIME))
    if [ "$ELAPSED_SECONDS" -ge "$MAX_WAIT" ]; then
        echo "Ant Ivy build failed: Timed out after $((MAX_WAIT / 60)) minutes." >&2
        exit 1
    fi
    echo "Ant Ivy build failed. Retrying immediately! (Elapsed: $ELAPSED_SECONDS/$MAX_WAIT seconds)"
done
echo "Ant Ivy build succeeded."
set -e

# Deploy Java code
cd /java
ant -f manager-build.xml -Ddeploy.mode=local refresh-branding-jar deploy
ant -f manager-build.xml apidoc-jsp
mkdir -p /usr/share/susemanager/www/tomcat/webapps/rhn/apidoc/
rsync -av /java/build/reports/apidocs/jsp/ /usr/share/susemanager/www/tomcat/webapps/rhn/apidoc/

# Build Web code
cd /
npm --prefix web ci --ignore-scripts --save=false --omit=dev
npm --prefix web run build -- --check-spec=false
rsync -a web/html/src/dist/ /usr/share/susemanager/www/htdocs/

# Deploy mgr-push
cp /client/tools/mgr-push/*.py /usr/lib/python3.6/site-packages/rhnpush/
cp /client/tools/mgr-push/rhnpushrc /etc/sysconfig/rhn/rhnpushrc

# Deploy Salt files
cd /susemanager-utils/susemanager-sls/
cp -R modules/* /usr/share/susemanager/modules
cp -R salt/* /usr/share/susemanager/salt
cp -R src/modules/* /usr/share/susemanager/salt/_modules
cp -R src/grains/* /usr/share/susemanager/salt/_grains
cp -R src/states/* /usr/share/susemanager/salt/_states
cp -R src/beacons/* /usr/share/susemanager/salt/_beacons
cp -R salt-ssh/* /usr/share/susemanager/salt-ssh

cd /susemanager/
cp src/mgr-salt-ssh /usr/bin/
chmod a+x /usr/bin/mgr-salt-ssh
cd src
cp mgr_sync/*.py /usr/lib/python3.6/site-packages/spacewalk/susemanager/mgr_sync/
cp *.py /usr/lib/python3.6/site-packages/spacewalk/susemanager/
mv /usr/lib/python3.6/site-packages/spacewalk/susemanager/mgr_bootstrap_data.py /usr/share/susemanager/
