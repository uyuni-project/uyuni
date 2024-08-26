#! /bin/sh
SCRIPT=$(basename ${0})

if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

while getopts 'P:h' option
do
    case ${option} in
        P) VPRODUCT="VERSION.${OPTARG}" ;;
        h) echo "Usage ${VERSION} [-P PRODUCT]";exit 2;;
    esac
done

HERE=`dirname $0`

if [ ! -f ${HERE}/${VPRODUCT} ];then
   echo "${VPRODUCT} does not exist"
   exit 3
fi

echo "Loading ${VPRODUCT}"
. ${HERE}/${VPRODUCT}
GITROOT=`readlink -f ${HERE}/../../../`

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/python/:/manager/client/rhel/spacewalk-client-tools/src"

SPACEWALK_FILES="
susemanager/src/
spacewalk/
python/
proxy/proxy/
proxy/installer/fetch-certificate.py
proxy/installer/rhn-proxy-activate.py
proxy/proxy/wsgi/xmlrpc.py
proxy/proxy/wsgi/xmlrpc_redirect.py
reporting/reports.py
utils/cloneByDate.py
utils/delete-old-systems-interactive
utils/depsolver.py
utils/__init__.py
utils/migrate-system-profile
utils/migrateSystemProfile.py
utils/spacewalk-api
utils/spacewalk-clone-by-date
utils/spacewalk-common-channels
utils/spacewalk-dump-schema
utils/spacewalk-export
utils/spacewalk-export-channels
utils/spacewalk-hostname-rename
utils/spacewalk-manage-channel-lifecycle
utils/spacewalk-manage-snapshots
utils/spacewalk-sync-setup
utils/spacewalk-utils.changes
utils/sw-ldap-user-sync
utils/sw-system-snapshot
utils/systemSnapshot.py
utils/taskotop
spacewalk/certs-tools/
spacewalk/setup/share/embedded_diskspace_check.py

client/debian/apt-spacewalk/packages.py
client/debian/apt-spacewalk/post_invoke.py
client/debian/apt-spacewalk/pre_invoke.py
client/rhel/spacewalk-client-tools/src/actions/
client/rhel/spacewalk-client-tools/src/bin/rhn_check.py
client/rhel/spacewalk-client-tools/src/up2date_client/
client/tools/spacewalk-client-cert/clientcert.py
client/tools/mgr-cfg/actions/
client/tools/mgr-cfg/config_client/
client/tools/mgr-cfg/config_common/
client/tools/mgr-cfg/config_management/
client/tools/mgr-push/

scripts/clone-errata/rhn-clone-errata.py
scripts/datasource-query-usage.py
scripts/devel/cobbler-api-example.py
scripts/find-unused-tags.py
scripts/link-tree.py
scripts/ncsu-rhntools/config.py
scripts/ncsu-rhntools/getRealmHosts.py
scripts/ncsu-rhntools/rhnapi.py
scripts/ncsu-rhntools/rhnstats/pysqlite.py
scripts/ncsu-rhntools/rhnstats/rhndb.py
scripts/ncsu-rhntools/rhnstats/rhnstats.py
scripts/ncsu-rhntools/rhnstats/schema.py
scripts/ncsu-rhntools/groupSummary.py
scripts/ncsu-rhntools/oldSystems.py
scripts/ncsu-rhntools/subscribeRHN.py
scripts/ncsu-rhntools/findpackages.py
scripts/gen-eclipse.py
scripts/update_symlinks.py
search-server/spacewalk-doc-indexes/create_urls_per_language.py
search-server/spacewalk-search/scripts/search.py
search-server/spacewalk-search/scripts/search.admin.updateIndex.test.py
spacecmd/src/spacecmd

tftpsync/susemanager-tftpsync/sync_post_tftpd_proxies.py
tftpsync/susemanager-tftpsync/MultipartPostHandler.py
susemanager-utils/performance/locust/00_Core_LoginAndSystemOverview.py
susemanager-utils/susemanager-sls/modules/
susemanager-utils/susemanager-sls/salt/channels/yum-susemanager-plugin/susemanagerplugin.py
susemanager-utils/susemanager-sls/src/
"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
PYLINT_CMD="mkdir -p /manager/reports; cd /manager/; pylint --disable=E0203,E0611,E1101,E1102,C0111,I0011,R0801 --ignore=test --output-format=parseable --rcfile /manager/susemanager-utils/testing/automation/spacewalk-pylint.rc --reports=y --msg-template=\"{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}\""
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/sh -c "${INITIAL_CMD}; $PYLINT_CMD `echo $SPACEWALK_FILES` > reports/pylint.log; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"

if [ $? -ne 0 ]; then
   EXIT=1
fi

exit $EXIT
