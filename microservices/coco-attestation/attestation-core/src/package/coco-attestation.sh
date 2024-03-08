#!/bin/sh
#
# Copyright (c) 2024 SUSE LLC
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

# CoCo Attestation Daemon start script
. /usr/share/coco-attestation/conf/daemon.conf

# Add additional options to /etc/rhn/coco-attestation.conf, if present
if [ -f /etc/coco-attestation.conf ]; then
    . /etc/coco-attestation.conf
fi

ATTESTATION_PARAMS="-Dfile.encoding=UTF-8 -Xms${ATTESTATION_INIT_MEMORY}m -Xmx${ATTESTATION_MAX_MEMORY}m ${ATTESTATION_CRASH_PARAMS} ${ATTESTATION_JAVA_OPTS}"
ATTESTATION_CLASSPATH="${ATTESTATION_CLASSES}:${ATTESTATION_JARS}"

/usr/bin/java -Djava.library.path="${ATTESTATION_LIBRARY_PATH}" \
    -classpath ${ATTESTATION_CLASSPATH} ${ATTESTATION_PARAMS} ${JAVA_OPTS} ${JAVA_AGENT} \
    com.suse.coco.CoCoAttestation
