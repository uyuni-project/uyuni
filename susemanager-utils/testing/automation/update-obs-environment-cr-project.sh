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

osc ls ${parent_project} > /dev/null
if [ ${?} -ne 0 ];then
    echo "Error. Does ${parent_project} exists?"
    exit -1
fi

update_project ${test_parent_project} ${parent_project} ${parent_repo_name}
    
