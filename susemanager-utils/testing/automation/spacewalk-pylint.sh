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
manager/susemanager/src/
manager/spacewalk/
manager/backend/
manager/client/rhel/rhnlib/rhn/
manager/proxy/proxy/
manager/proxy/installer/fetch-certificate.py
manager/proxy/installer/rhn-proxy-activate.py
manager/proxy/proxy/wsgi/xmlrpc.py
manager/proxy/proxy/wsgi/xmlrpc_redirect.py
manager/reporting/reports.py
manager/susemanager-utils/susemanager-sls/src/
manager/utils/
manager/spacewalk/certs-tools/
manager/spacewalk/setup/share/embedded_diskspace_check.py
manager/suseRegisterInfo/suseRegister/

manager/client/debian/apt-spacewalk/packages.py
manager/client/debian/apt-spacewalk/post_invoke.py
manager/client/debian/apt-spacewalk/pre_invoke.py
manager/client/rhel/yum-rhn-plugin/actions/errata.py
manager/client/rhel/yum-rhn-plugin/actions/packages.py
manager/client/rhel/yum-rhn-plugin/rhnplugin.py
manager/client/rhel/dnf-plugin-spacewalk/actions/errata.py
manager/client/rhel/dnf-plugin-spacewalk/actions/packages.py
manager/client/rhel/dnf-plugin-spacewalk/spacewalk.py
manager/client/rhel/spacewalk-client-tools/src/actions/
manager/client/rhel/spacewalk-client-tools/src/bin/rhn-profile-sync.py
manager/client/rhel/spacewalk-client-tools/src/bin/rhn_check.py
manager/client/rhel/spacewalk-client-tools/src/bin/rhn_register.py
manager/client/rhel/spacewalk-client-tools/src/bin/rhnreg_ks.py
manager/client/rhel/spacewalk-client-tools/src/bin/spacewalk-channel.py
manager/client/rhel/spacewalk-client-tools/src/bin/spacewalk-update-status.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_choose_channel.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_choose_server_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_create_profile_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_finish_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_login_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_provide_certificate_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_register.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_review_gui.py
manager/client/rhel/spacewalk-client-tools/src/firstboot-legacy-rhel6/rhn_start_gui.py
manager/client/rhel/spacewalk-client-tools/src/up2date_client/
manager/client/tools/spacewalk-abrt/src/spacewalk_abrt/
manager/client/tools/spacewalk-koan/actions/kickstart.py
manager/client/tools/spacewalk-koan/actions/kickstart_guest.py
manager/client/tools/spacewalk-koan/spacewalkkoan/
manager/client/tools/spacewalk-client-cert/clientcert.py
manager/client/tools/spacewalk-oscap/scap.py
manager/client/tools/mgr-cfg/actions/
manager/client/tools/mgr-cfg/config_client/
manager/client/tools/mgr-cfg/config_common/
manager/client/tools/mgr-cfg/config_management/
manager/client/tools/mgr-custom-info/rhn-custom-info.py
manager/client/tools/mgr-osad/invocation.py
manager/client/tools/mgr-osad/src/
manager/client/tools/mgr-push/
manager/client/tools/mgr-virtualization/actions/image.py
manager/client/tools/mgr-virtualization/actions/virt.py
manager/client/tools/mgr-virtualization/virtualization/

manager/scripts/clone-errata/rhn-clone-errata.py
manager/scripts/datasource-query-usage.py
manager/scripts/devel/cobbler-api-example.py
manager/scripts/find-unused-tags.py
manager/scripts/link-tree.py
manager/scripts/ncsu-rhntools/config.py
manager/scripts/ncsu-rhntools/getRealmHosts.py
manager/scripts/ncsu-rhntools/rhnapi.py
manager/scripts/ncsu-rhntools/rhnstats/pysqlite.py
manager/scripts/ncsu-rhntools/rhnstats/rhndb.py
manager/scripts/ncsu-rhntools/rhnstats/rhnstats.py
manager/scripts/ncsu-rhntools/rhnstats/schema.py
manager/scripts/ncsu-rhntools/groupSummary.py
manager/scripts/ncsu-rhntools/oldSystems.py
manager/scripts/ncsu-rhntools/subscribeRHN.py
manager/scripts/ncsu-rhntools/findpackages.py
manager/scripts/gen-eclipse.py
manager/scripts/update_symlinks.py
manager/search-server/spacewalk-doc-indexes/create_urls_per_language.py
manager/search-server/spacewalk-search/scripts/search.py
manager/search-server/spacewalk-search/scripts/search.admin.updateIndex.test.py
manager/spacecmd/src/lib/activationkey.py
manager/spacecmd/src/lib/argumentparser.py
manager/spacecmd/src/lib/configchannel.py
manager/spacecmd/src/lib/distribution.py
manager/spacecmd/src/lib/kickstart.py
manager/spacecmd/src/lib/misc.py
manager/spacecmd/src/lib/package.py
manager/spacecmd/src/lib/api.py
manager/spacecmd/src/lib/cryptokey.py
manager/spacecmd/src/lib/custominfo.py
manager/spacecmd/src/lib/errata.py
manager/spacecmd/src/lib/filepreservation.py
manager/spacecmd/src/lib/org.py
manager/spacecmd/src/lib/repo.py
manager/spacecmd/src/lib/report.py
manager/spacecmd/src/lib/scap.py
manager/spacecmd/src/lib/schedule.py
manager/spacecmd/src/lib/shell.py
manager/spacecmd/src/lib/snippet.py
manager/spacecmd/src/lib/ssm.py
manager/spacecmd/src/lib/system.py
manager/spacecmd/src/lib/user.py
manager/spacecmd/src/lib/utils.py
manager/spacecmd/src/lib/group.py
manager/spacecmd/src/lib/softwarechannel.py

manager/tftpsync/susemanager-tftpsync/sync_post_tftpd_proxies.py
manager/tftpsync/susemanager-tftpsync/MultipartPostHandler.py
manager/usix/
manager/susemanager-utils/nagios-plugin/check_suma_common.py
manager/susemanager-utils/performance/locust/00_Core_LoginAndSystemOverview.py
manager/susemanager-utils/susemanager-sls/modules/pillar/suma_minion.py
manager/susemanager-utils/susemanager-sls/modules/runners/mgrk8s.py
manager/susemanager-utils/susemanager-sls/modules/runners/kiwi-image-collect.py
manager/susemanager-utils/susemanager-sls/modules/runners/mgrutil.py
manager/susemanager-utils/susemanager-sls/modules/tops/mgr_master_tops.py
manager/susemanager-utils/susemanager-sls/salt/channels/yum-susemanager-plugin/susemanagerplugin.py
manager/susemanager-utils/susemanager-sls/src/
"

PYLINT_CMD="pylint --disable=E0203,E0611,E1101,E1102,C0111,I0011,R0801 --ignore=test --output-format=parseable --rcfile /manager/spacewalk/pylint/spacewalk-pylint.rc --reports=y --msg-template=\"{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}\""

docker pull $REGISTRY/$PYLINT_CONTAINER
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/sh -c "mkdir -p /manager/reports; $PYLINT_CMD `echo $SPACEWALK_FILES` > /manager/reports/pylint.log || :"

if [ $? -ne 0 ]; then
   EXIT=1
fi

rm -f $GITROOT/backend/common/usix.py*

exit $EXIT
