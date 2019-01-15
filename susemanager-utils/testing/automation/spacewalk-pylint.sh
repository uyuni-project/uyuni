#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# fake usix.py
if [ ! -e $GITROOT/backend/common/usix.py ]; then
    ln -sf ../../usix/common/usix.py $GITROOT/backend/common/usix.py
fi

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/client/rhel/rhn-client-tools/src"

SPACEWALK_FILES="
susemanager/src/
spacewalk/
backend/
client/rhel/rhnlib/rhn/
proxy/proxy/
proxy/installer/fetch-certificate.py
proxy/installer/rhn-proxy-activate.py
proxy/proxy/wsgi/xmlrpc.py
proxy/proxy/wsgi/xmlrpc_redirect.py
reporting/reports.py
susemanager-utils/susemanager-sls/src/
utils/apply_errata
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
utils/spacewalk-final-archive
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
suseRegisterInfo/suseRegister/

client/debian/apt-spacewalk/packages.py
client/debian/apt-spacewalk/post_invoke.py
client/debian/apt-spacewalk/pre_invoke.py
client/rhel/yum-rhn-plugin/actions/errata.py
client/rhel/yum-rhn-plugin/actions/packages.py
client/rhel/yum-rhn-plugin/rhnplugin.py
client/rhel/dnf-plugin-spacewalk/actions/errata.py
client/rhel/dnf-plugin-spacewalk/actions/packages.py
client/rhel/dnf-plugin-spacewalk/spacewalk.py
client/rhel/spacewalk-client-tools/src/actions/
client/rhel/spacewalk-client-tools/src/bin/rhn-profile-sync.py
client/rhel/spacewalk-client-tools/src/bin/rhn_check.py
client/rhel/spacewalk-client-tools/src/bin/rhn_register.py
client/rhel/spacewalk-client-tools/src/bin/rhnreg_ks.py
client/rhel/spacewalk-client-tools/src/bin/spacewalk-channel.py
client/rhel/spacewalk-client-tools/src/bin/spacewalk-update-status.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_choose_channel.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_choose_server_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_create_profile_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_finish_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_login_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_provide_certificate_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_register.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_review_gui.py
client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_start_gui.py
client/rhel/spacewalk-client-tools/src/up2date_client/
client/tools/spacewalk-abrt/src/spacewalk_abrt/
client/tools/spacewalk-koan/actions/kickstart.py
client/tools/spacewalk-koan/actions/kickstart_guest.py
client/tools/spacewalk-koan/spacewalkkoan/
client/tools/spacewalk-client-cert/clientcert.py
client/tools/spacewalk-oscap/scap.py
client/tools/mgr-cfg/actions/
client/tools/mgr-cfg/config_client/
client/tools/mgr-cfg/config_common/
client/tools/mgr-cfg/config_management/
client/tools/mgr-custom-info/rhn-custom-info.py
client/tools/mgr-osad/invocation.py
client/tools/mgr-osad/src/
client/tools/mgr-push/
client/tools/mgr-virtualization/actions/image.py
client/tools/mgr-virtualization/actions/virt.py
client/tools/mgr-virtualization/virtualization/

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
spacecmd/src/lib/activationkey.py
spacecmd/src/lib/argumentparser.py
spacecmd/src/lib/configchannel.py
spacecmd/src/lib/distribution.py
spacecmd/src/lib/kickstart.py
spacecmd/src/lib/misc.py
spacecmd/src/lib/package.py
spacecmd/src/lib/api.py
spacecmd/src/lib/cryptokey.py
spacecmd/src/lib/custominfo.py
spacecmd/src/lib/errata.py
spacecmd/src/lib/filepreservation.py
spacecmd/src/lib/org.py
spacecmd/src/lib/repo.py
spacecmd/src/lib/report.py
spacecmd/src/lib/scap.py
spacecmd/src/lib/schedule.py
spacecmd/src/lib/shell.py
spacecmd/src/lib/snippet.py
spacecmd/src/lib/ssm.py
spacecmd/src/lib/system.py
spacecmd/src/lib/user.py
spacecmd/src/lib/utils.py
spacecmd/src/lib/group.py
spacecmd/src/lib/softwarechannel.py

tftpsync/susemanager-tftpsync/sync_post_tftpd_proxies.py
tftpsync/susemanager-tftpsync/MultipartPostHandler.py
usix/
susemanager-utils/nagios-plugin/check_suma_common.py
susemanager-utils/performance/locust/00_Core_LoginAndSystemOverview.py
susemanager-utils/susemanager-sls/modules/pillar/suma_minion.py
susemanager-utils/susemanager-sls/modules/runners/mgrk8s.py
susemanager-utils/susemanager-sls/modules/runners/kiwi-image-collect.py
susemanager-utils/susemanager-sls/modules/runners/mgrutil.py
susemanager-utils/susemanager-sls/modules/tops/mgr_master_tops.py
susemanager-utils/susemanager-sls/salt/channels/yum-susemanager-plugin/susemanagerplugin.py
susemanager-utils/susemanager-sls/src/
"

PYLINT_CMD="pylint --disable=E0203,E0611,E1101,E1102,C0111,I0011,R0801 --ignore=test --output-format=parseable --rcfile /manager/spacewalk/pylint/spacewalk-pylint.rc --reports=y --msg-template=\"{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}\""

docker pull $REGISTRY/$PYLINT_CONTAINER
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/sh -c "mkdir -p /manager/reports; cd /manager/; $PYLINT_CMD `echo $SPACEWALK_FILES` > reports/pylint.log || :"

if [ $? -ne 0 ]; then
   EXIT=1
fi

rm -f $GITROOT/backend/common/usix.py*

exit $EXIT
