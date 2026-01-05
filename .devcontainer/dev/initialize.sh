#!/usr/bin/env bash
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

WORKSPACE=$1
if [ -z "$WORKSPACE" ]; then
    echo "ERROR: No workspacefolder specified" 1>&2
    exit 1
fi

if [ ! -d "$WORKSPACE" ] || [ ! -x "$WORKSPACE" ]; then
    echo "ERROR: Directory $WORKSPACE does not exist or is not accessible" 1>&2
    exit 1
fi

cd $WORKSPACE

# Install the parent pom
mvn -f microservices/uyuni-java-parent --non-recursive install

# Install branding so we can compile spacewalk-java alone
mvn -f branding install

# Initialize spacewalk-java so we donwload all the dependencies
mvn -f java --non-recursive initialize

# Install frontend dependencies
npm --prefix web install
