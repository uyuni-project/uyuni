#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# we need a special (old) baseimage to migrate to current schema
docker pull $REGISTRY/suma-2.1-pgsql
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/suma-2.1-pgsql /manager/susemanager-utils/testing/docker/scripts/schema_migration_test_pgsql-21to31.sh
