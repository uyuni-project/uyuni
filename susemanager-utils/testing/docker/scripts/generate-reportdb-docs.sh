#!/bin/bash

#
# Copyright (c) 2022 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
set -e
SCRIPT=$(basename "$0")

if [ -z "$1" ];then
    echo "Missing parameters"
    echo "Usage: $SCRIPT brand_name"
fi

BRAND_NAME="$1"

if [ "$BRAND_NAME" == "unittest" ];then
    echo "Running just unit test"
fi

echo "Using branding $BRAND_NAME"

cd /manager/susemanager-utils/testing/docker/scripts/

# Move Postgres database to tmpfs to speed initialization and testing up
if [ -n "$PG_TMPFS_DIR" ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh "$PG_TMPFS_DIR"
fi

echo "Going to reset pgsql database"

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/:/manager/schema/spacewalk/lib
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

export SYSTEMD_NO_WRAP=1

su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl start" ||:

# this copy the latest schema from the git into the system
cp -r /manager/schema /tmp
pushd /tmp/schema/reportdb

RPM_VERSION=$(rpm -q --qf "%{version}\n" --specfile uyuni-reportdb-schema.spec | head -n 1)
NEXT_VERSION=$(echo "$RPM_VERSION" | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\.","",post); print pre post+1 ; }')

if [ -d "upgrade/uyuni-reportdb-schema-$RPM_VERSION-to-uyuni-reportdb-schema-$NEXT_VERSION" ]; then
    DB_VERSION=$NEXT_VERSION
else
    DB_VERSION=$RPM_VERSION
fi

# Create the schema files
echo "Creating schema creation file"
make -s -f Makefile.schema SCHEMA=uyuni-reportdb-schema VERSION="$DB_VERSION" RELEASE=testing BRAND_NAME="$BRAND_NAME"
# Create the documentation addons
echo "Creating schema documentation"
make -s -f Makefile.schema docs

# Unit test for checking if reportdb schema and doc are aligned
SCHEMA_DIFF=$(./check_reportdb_doc)
popd

if [ -n "$SCHEMA_DIFF" ]; then
        echo "ReportDB schema and doc are misaligned"
        echo "$SCHEMA_DIFF"
        exit 1
fi
echo "ReportDB schema and doc are aligned"


if [ "$BRAND_NAME" == "unittest" ];then
  exit 0
fi

# Create the schema
cd /manager/schema

cp /root/rhn.conf /etc/rhn/rhn.conf

cd spacewalk
./spacewalk-sql --reportdb /tmp/schema/reportdb/postgres/main.sql
./spacewalk-sql --reportdb /tmp/schema/reportdb/postgres/documentation.sql

cd ../reportdb

CONFIG_FILE=/root/reportdb-docs.properties

OUTPUT_FILE=reportdb-schema-docs.tar.xz
if [ -f $OUTPUT_FILE ]; then
    echo "Removing previous generated tarball $OUTPUT_FILE"
    rm -f $OUTPUT_FILE
fi

OUTPUT_DIR=$(grep schemaspy.o $CONFIG_FILE | cut -c 13-)
if [ -d "$OUTPUT_DIR" ]; then
    echo "Removing previous generated docs at $OUTPUT_DIR"
    rm -rf "$OUTPUT_DIR"
fi

SCHEMASPY_REPO=mackdk/schemaspy
SCHEMASPY_VERSION=6.1.1
SCHEMASPY_JAR=/root/schemaspy.jar
if [ ! -f $SCHEMASPY_JAR ]; then
    echo "Retrieving SchemaSpy version $SCHEMASPY_VERSION"
    wget -q --show-progress "https://github.com/$SCHEMASPY_REPO/releases/download/v$SCHEMASPY_VERSION/schemaspy-$SCHEMASPY_VERSION.jar" -O $SCHEMASPY_JAR
fi

# Check postgresql runtime dependency. If version is >= 42.2.19, we need ongres-stringprep as well
POSTGRESQL_VERSION=$(rpm -q --qf '%{V}\n' postgresql-jdbc)
if echo -e "$POSTGRESQL_VERSION\n42.2.19" | sort --reverse --version-sort --check=quiet; then
    DRIVER_PATH=$(build-classpath postgresql-jdbc ongres-scram ongres-stringprep)
else
    DRIVER_PATH=$(build-classpath postgresql-jdbc ongres-scram)
fi

java -jar $SCHEMASPY_JAR -configFile $CONFIG_FILE -dp "$DRIVER_PATH" -label "$BRAND_NAME Reporting"

CSS_FILE=$(grep schemaspy.css $CONFIG_FILE | cut -c 15-)
if [ -n "$CSS_FILE" ] && [ -f "$CSS_FILE" ]; then
    echo "Copying CSS file $CSS_FILE"
    cp "$CSS_FILE" out/schemaSpy.css
fi

echo "Building docs distribution tarball $OUTPUT_FILE"
tar cJf $OUTPUT_FILE out/ --transform s/out/reportdb-schema/

rm -rf "$OUTPUT_DIR"
