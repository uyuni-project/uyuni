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

# yaml file to be generated
GENERATED_YAML_FILE="$JAVA_SOURCES_DIR/rewrite.yml"

# list of test packages
TEST_PACKAGES=$(grep -rh --include=*.java "^package .*test;" $JAVA_SOURCES_DIR |cut -d' ' -f2|cut -d';' -f1|uniq)



#remove old yaml file if any
rm -f $GENERATED_YAML_FILE

#generate yaml file: header
echo "---" >> $GENERATED_YAML_FILE
echo "type: specs.openrewrite.org/v1beta/recipe" >> $GENERATED_YAML_FILE
echo "name: com.suse.MigrateTestPackages" >> $GENERATED_YAML_FILE
echo "displayName: Rename test packages" >> $GENERATED_YAML_FILE
echo "recipeList:" >> $GENERATED_YAML_FILE

#generate yaml file: body
COUNT=0

for i in $TEST_PACKAGES
do
   echo "  - org.openrewrite.java.ChangePackage:" >> $GENERATED_YAML_FILE
   echo "      oldPackageName: ""$i" >> $GENERATED_YAML_FILE
   echo "      newPackageName: ""${i%.*}" >> $GENERATED_YAML_FILE

   COUNT=$((COUNT+1))
done

echo $COUNT


