#!/bin/bash
set -xe

src_dir=$(cd "$(dirname "$0")/../.." && pwd -P)
PAYLOAD_DIR="${src_dir}/.build/acceptance-server-root"
mkdir -p "${PAYLOAD_DIR}"

# Build the sources
cd "${src_dir}"
echo "Building Java parent..."
mvn -f microservices/uyuni-java-parent/ install --non-recursive

echo "Building Java branding..."
mvn -f branding/ install

echo "Building Java..."
mvn -f java initialize

echo "Building Frontend..."
npm --prefix web ci --ignore-scripts --save=false --omit=dev
npm --prefix web run build -- --check-spec=false

echo "Assembling Payload..."
rm -rf "${PAYLOAD_DIR:?}"
mkdir -p "${PAYLOAD_DIR}"
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn"
mkdir -p "${PAYLOAD_DIR}/usr/share/rhn/lib"
mkdir -p "${PAYLOAD_DIR}/usr/share/spacewalk/taskomatic"
SPACEWALK_JAVA_VERSION=$(cat java/pom.xml | grep -m 1 "<version>" | sed -e 's/<[^>]*>//g' -e 's/^[ \t]*//')
BRANDING_VERSION=$(cat branding/pom.xml | grep -m 1 "<version>" | sed -e 's/<[^>]*>//g' -e 's/^[ \t]*//')

rsync -a "java/webapp/target/webapp-${SPACEWALK_JAVA_VERSION}/" "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/"

mv "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/branding-${BRANDING_VERSION}.jar" "${PAYLOAD_DIR}/usr/share/rhn/lib/java-branding.jar"
ln -sf /usr/share/rhn/lib/java-branding.jar "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/java-branding.jar"

mv "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/core-${SPACEWALK_JAVA_VERSION}.jar" "${PAYLOAD_DIR}/usr/share/rhn/lib/rhn.jar"
ln -sf /usr/share/rhn/lib/rhn.jar "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/rhn.jar"

# Symlinks for Taskomatic
for jar in "${PAYLOAD_DIR}/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/"*.jar; do
    jar_name=$(basename "$jar")
    ln -sf "/usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/lib/${jar_name}" "${PAYLOAD_DIR}/usr/share/spacewalk/taskomatic/${jar_name}"
done

# Frontend dist
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/www/htdocs"
rsync -a "web/html/src/dist/" "${PAYLOAD_DIR}/usr/share/susemanager/www/htdocs/"

# Python and Salt files
mkdir -p "${PAYLOAD_DIR}/payload-python/rhnpush"
cp -r client/tools/mgr-push/*.py "${PAYLOAD_DIR}/payload-python/rhnpush/"
mkdir -p "${PAYLOAD_DIR}/etc/sysconfig/rhn"
cp client/tools/mgr-push/rhnpushrc "${PAYLOAD_DIR}/etc/sysconfig/rhn/rhnpushrc"

mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager"
cp -R susemanager-utils/susemanager-sls/modules "${PAYLOAD_DIR}/usr/share/susemanager/"
cp -R susemanager-utils/susemanager-sls/salt "${PAYLOAD_DIR}/usr/share/susemanager/"
cp -R susemanager-utils/susemanager-sls/reactor "${PAYLOAD_DIR}/usr/share/susemanager/"
cp -R susemanager-utils/susemanager-sls/formulas "${PAYLOAD_DIR}/usr/share/susemanager/"
cp -R susemanager-utils/susemanager-sls/scap "${PAYLOAD_DIR}/usr/share/susemanager/"
cp -R susemanager-utils/susemanager-sls/src/modules "${PAYLOAD_DIR}/usr/share/susemanager/salt/_modules"
cp -R susemanager-utils/susemanager-sls/src/grains "${PAYLOAD_DIR}/usr/share/susemanager/salt/_grains"
cp -R susemanager-utils/susemanager-sls/src/states "${PAYLOAD_DIR}/usr/share/susemanager/salt/_states"
cp -R susemanager-utils/susemanager-sls/src/beacons "${PAYLOAD_DIR}/usr/share/susemanager/salt/_beacons"
cp -R susemanager-utils/susemanager-sls/salt-ssh "${PAYLOAD_DIR}/usr/share/susemanager/salt-ssh"

mkdir -p "${PAYLOAD_DIR}/srv/formula_metadata"
cp -R susemanager-utils/susemanager-sls/formula_metadata/* "${PAYLOAD_DIR}/srv/formula_metadata/"

mkdir -p "${PAYLOAD_DIR}/usr/bin"
cp susemanager/src/mgr-salt-ssh "${PAYLOAD_DIR}/usr/bin/"
chmod a+x "${PAYLOAD_DIR}/usr/bin/mgr-salt-ssh"

mkdir -p "${PAYLOAD_DIR}/payload-python/spacewalk/susemanager/mgr_sync"
cp susemanager/src/mgr_sync/*.py "${PAYLOAD_DIR}/payload-python/spacewalk/susemanager/mgr_sync/"
cp susemanager/src/*.py "${PAYLOAD_DIR}/payload-python/spacewalk/susemanager/"
mv "${PAYLOAD_DIR}/payload-python/spacewalk/susemanager/mgr_bootstrap_data.py" "${PAYLOAD_DIR}/usr/share/susemanager/"

# Schema payload
echo "Building Spacewalk main database schema..."
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/db/postgres"
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/db/schema-upgrade"
mkdir -p "${PAYLOAD_DIR}/usr/bin"
cp schema/spacewalk/spacewalk-schema-upgrade "${PAYLOAD_DIR}/usr/bin/"

cd schema/spacewalk
SCHEMA_VERSION=$(grep -i "^Version:" susemanager-schema.spec | awk '{print $2}')
SCHEMA_RELEASE=$(grep -i "^Release:" susemanager-schema.spec | awk '{print $2}')
make -f Makefile.schema SCHEMA=susemanager-schema VERSION="${SCHEMA_VERSION}" RELEASE="${SCHEMA_RELEASE}"

install -m 0644 postgres/main.sql "${PAYLOAD_DIR}/usr/share/susemanager/db/postgres/"
install -m 0644 postgres/end.sql "${PAYLOAD_DIR}/usr/share/susemanager/db/postgres/upgrade-end.sql"
( cd upgrade && tar cf - --exclude='*.sql' . | ( cd "${PAYLOAD_DIR}/usr/share/susemanager/db/schema-upgrade" && tar xf - ) )
cd "${src_dir}"

echo "Building Report Database schema..."
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/db/reportdb"
mkdir -p "${PAYLOAD_DIR}/usr/share/susemanager/db/reportdb-schema-upgrade"

cd schema/reportdb
REPORTDB_VERSION=$(grep -i "^Version:" uyuni-reportdb-schema.spec | awk '{print $2}')
# Export PATH containing schema-source-sanity-check.pl (Finding 2)
export PATH="${src_dir}/schema/spacewalk:${PATH}"
make -f Makefile.schema SCHEMA=uyuni-reportdb-schema VERSION="${REPORTDB_VERSION}" RELEASE=0

install -m 0644 postgres/main.sql "${PAYLOAD_DIR}/usr/share/susemanager/db/reportdb/"
install -m 0644 postgres/end.sql "${PAYLOAD_DIR}/usr/share/susemanager/db/reportdb/upgrade-end.sql"
( cd upgrade && tar cf - --exclude='*.sql' . | ( cd "${PAYLOAD_DIR}/usr/share/susemanager/db/reportdb-schema-upgrade" && tar xf - ) )
cd "${src_dir}"

# Entrypoint and startup scripts
cp -r containers/server-image/root/* "${PAYLOAD_DIR}/"
chmod a+x "${PAYLOAD_DIR}/docker-entrypoint-init"
chmod a+x "${PAYLOAD_DIR}/usr/bin/healthcheck.sh"
chmod a+x "${PAYLOAD_DIR}/usr/bin/liveness-check.sh"
chmod a+x "${PAYLOAD_DIR}/usr/bin/startup-check.sh"
chmod a+x "${PAYLOAD_DIR}/usr/bin/timezone_alignment.sh"
chmod a+x "${PAYLOAD_DIR}/usr/bin/uyuni-configfiles-sync"

mkdir -p "${PAYLOAD_DIR}/usr/sbin"
cp spacewalk/admin/spacewalk-startup-helper "${PAYLOAD_DIR}/usr/sbin/spacewalk-startup-helper"
chmod a+x "${PAYLOAD_DIR}/usr/sbin/spacewalk-startup-helper"

mkdir -p "${PAYLOAD_DIR}/etc/tomcat/conf.d"
cp spacewalk/setup/share/tomcat_java_opts.conf "${PAYLOAD_DIR}/etc/tomcat/conf.d/"
cp spacewalk/setup/share/tomcat_java_opts_suse.conf "${PAYLOAD_DIR}/etc/tomcat/conf.d/"

mkdir -p "${PAYLOAD_DIR}/usr/share/rhn/config-defaults"
cp java/conf/default/rhn_taskomatic_daemon.conf "${PAYLOAD_DIR}/usr/share/rhn/config-defaults/"

cp spacewalk/setup/bin/spacewalk-setup "${PAYLOAD_DIR}/usr/bin/"
chmod a+x "${PAYLOAD_DIR}/usr/bin/spacewalk-setup"

mkdir -p "${PAYLOAD_DIR}/payload-setup"
cp spacewalk/setup/lib/Spacewalk/Setup.pm "${PAYLOAD_DIR}/payload-setup/"

mkdir -p "${PAYLOAD_DIR}/payload-certs"
cp spacewalk/certs-tools/mgr_ssl_cert_setup.py "${PAYLOAD_DIR}/payload-certs/"

cp testsuite/podman_runner/debug_logging.properties "${PAYLOAD_DIR}/etc/tomcat/logging.properties"

echo "Payload assembled successfully in ${PAYLOAD_DIR}"

# Archive to tarball to preserve permissions/symlinks
echo "Creating pr changes payload tarball archive..."
cd "${src_dir}/.build"
tar -czf pr-changes-payload.tar.gz -C acceptance-server-root .
echo "Payload tarball created successfully at ${src_dir}/.build/pr-changes-payload.tar.gz"
