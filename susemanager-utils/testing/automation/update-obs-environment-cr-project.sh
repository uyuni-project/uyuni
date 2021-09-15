#!/bin/bash

parent_project="systemsmanagement:Uyuni:Master"

usage_and_exit() {
    echo "usage: $0 N [test_project]"
    echo "where N is the environment"
    echo "and project is the test_project, which by default is ${parent_project}:N:CR" 
    echo "$0 will add new packages from ${parent_project} to test_project"
    exit -1
}

update_project() {
    tproject=${1}
    pproject=${2}
    rname=${3}
    echo "Updating ${tproject} from ${pproject} with repo ${rname}"
    osc ls ${tproject} > /dev/null
    if [ ${?} -ne 0 ];then
        echo "Project ${tproject} does not exists. Creating ..."
        echo ${new_project_config} |\
            sed -e "s/__TEST_PROJECT_NAME__/${tproject}/g" |\
            sed -e "s/__PARENT_PROJECT_NAME__/${pproject}/g" |\
            sed -e "s/__REPO_NAME__/${rname}/g" > ${OUT}
       osc meta prj --file=${OUT} -m "Created by ${0}" ${tproject}
    fi
    for i in $(diff <( osc ls ${tproject} ) <( osc ls ${pproject} ) | grep ">" | cut -d" " -f2);do
        echo "Found new package ${i} in ${pproject}. Creating a link to ${tproject}"
        osc linkpac ${pproject} ${i} ${tproject}
    done
}

if [ $# -lt 1 ];then
    usage_and_exit
fi
if [ $# -gt 2 ];then
    usage_and_exit
fi

test_parent_project="${parent_project}:TEST:${1}:CR"

if [ $# -eq 2 ];then
    test_parent_project=${2}
fi

parent_repo_name="openSUSE_Leap_15.3"

new_project_config="
    <project name=\"__TEST_PROJECT_NAME__\">
  <title>Continuous Rebuild project for environment ${1}</title>
  <description>This links to __PARENT_PROJECT_NAME RPMs. It will be disabled before each CI run, so there are no rebuilds during the CI build. Then, it will be enabled back to get the latest updates.</description>
  <person userid=\"jordimassaguerpla\" role=\"maintainer\"/>
  <person userid=\"juliogonzalezgil\" role=\"maintainer\"/>
  <person userid=\"zypp-team\" role=\"maintainer\"/>
  <repository name=\"__REPO_NAME__\">
    <path project=\"__PARENT_PROJECT_NAME__\" repository=\"__REPO_NAME__\"/>
    <arch>x86_64</arch>
  </repository>
</project>
"

osc ls ${parent_project} > /dev/null
if [ ${?} -ne 0 ];then
    echo "Error. Does ${parent_project} exists?"
    exit -1
fi

OUT="$(mktemp)"

update_project ${test_parent_project} ${parent_project} ${parent_repo_name}
    
