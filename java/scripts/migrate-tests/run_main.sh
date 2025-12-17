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

# ===============================================
# MAIN SCRIPT TO RUN TO MIGRATE TESTS
# ===============================================

set -e

# ===============================================
# variables and working dir
# ===============================================

# Absolute path to this script
THIS_SCRIPT=$(readlink -f $0)
# Absolute path this script is in
THIS_SCRIPTPATH=`dirname $THIS_SCRIPT`

# path of java sources
JAVA_SOURCES_DIR="${THIS_SCRIPTPATH%/scripts/migrate-tests}"

# yaml file to be generated
GENERATED_YAML_FILE="$JAVA_SOURCES_DIR/rewrite.yml"

# ===============================================
# preliminary steps
# ===============================================

echo ========== change dir to java/
cd $JAVA_SOURCES_DIR

echo ========== apply preliminary step patches
git apply "$THIS_SCRIPTPATH/preliminary_step_fix_test_resource_dirs_in_test_code.patch"
git apply "$THIS_SCRIPTPATH/preliminary_step_modify_test_queries.patch"


# ===============================================
# migrate tests
# ===============================================

echo ==========  patch pom file to include rewrite-maven-plugin
git apply "$THIS_SCRIPTPATH/migrate_tests_pom.patch"

echo ========== generate plugin yaml file
/bin/bash "$THIS_SCRIPTPATH/generate_rewrite_yml.sh"

echo ========== run the test migration
mvn rewrite:run

echo ========== remove yaml file
rm -f $GENERATED_YAML_FILE

echo ========== restore original pom file
git checkout pom.xml

echo ========== restructure resources
/bin/bash "$THIS_SCRIPTPATH/restructure_resources.sh"

# ===============================================
# post steps
# ===============================================

echo ========== apply post step patches
git apply "$THIS_SCRIPTPATH/post_step_rearrange_wrong_order_imports.patch"


echo ========== print and remove rej files
find . | grep ".rej"
find . | grep ".rej" | xargs rm
