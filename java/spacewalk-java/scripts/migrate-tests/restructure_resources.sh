#!/bin/bash
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

set -e

# Absolute path to this script
THIS_SCRIPT=$(readlink -f $0)
# Absolute path this script is in
THIS_SCRIPTPATH=`dirname $THIS_SCRIPT`

# path of java sources
JAVA_SOURCES_DIR="${THIS_SCRIPTPATH%/scripts/migrate-tests}"

# path of java test resources
JAVA_RESOURCES_DIR="$JAVA_SOURCES_DIR/core/src/test/resources"

#resources to be moved
RESOURCE_DIRS=(
"com/redhat/rhn/common/client/test"
"com/redhat/rhn/common/conf/test"
"com/redhat/rhn/common/localization/test"
"com/redhat/rhn/common/util/manifestfactory/test"
"com/redhat/rhn/common/util/test"
"com/redhat/rhn/common/validator/test"
"com/redhat/rhn/domain/kickstart/builder/test"
"com/redhat/rhn/domain/kickstart/crypto/test"
"com/redhat/rhn/frontend/action/user/test"
"com/redhat/rhn/frontend/nav/test"
"com/redhat/rhn/frontend/taglibs/test"
"com/redhat/rhn/frontend/xmlrpc/admin/configuration/test"
"com/redhat/rhn/frontend/xmlrpc/kickstart/test"
"com/redhat/rhn/frontend/xmlrpc/proxy/test"
"com/redhat/rhn/manager/audit/test"
"com/redhat/rhn/manager/content/test"
"com/redhat/rhn/manager/formula/test"
"com/redhat/rhn/manager/kickstart/cobbler/test"
"com/redhat/rhn/manager/system/test"
"com/redhat/rhn/taskomatic/task/gatherer/test"
"com/redhat/rhn/taskomatic/task/sshservice/test"
"com/redhat/rhn/taskomatic/task/test"
"com/suse/cloud/test"
"com/suse/manager/errata/advisorymap/test"
"com/suse/manager/gatherer/test"
"com/suse/manager/kubernetes/test"
"com/suse/manager/maintenance/test"
"com/suse/manager/reactor/messaging/test"
"com/suse/manager/reactor/test"
"com/suse/manager/reactor/utils/test"
"com/suse/manager/webui/controllers/test"
"com/suse/manager/webui/services/impl/test"
"com/suse/manager/webui/utils/salt/test"
"com/suse/manager/webui/utils/test"
"com/suse/oval/config/test"
"com/suse/scc/test"
"com/suse/utils/test"
"org/cobbler/test"
)



cd $JAVA_RESOURCES_DIR

#remove file
rm com/redhat/rhn/manager/content/test/.gitignore

COUNT=0

for dir in ${RESOURCE_DIRS[@]}
do
   NEW_RESOURCE_DIR=$(echo $dir | sed 's/test//')

   mv $dir/* $NEW_RESOURCE_DIR 2>/dev/null || true
   mv $dir/.* $NEW_RESOURCE_DIR 2>/dev/null || true
   rmdir $dir

   COUNT=$((COUNT+1))
done

echo $COUNT
cd -



