#!/bin/bash

parent_project="systemsmanagement:Uyuni:Master"

usage_and_exit() {
    echo "usage: $0 N [test_project]"
    echo "where N is the environment"
    echo "and project is the test_project, which by default is ${parent_project}:N:CR" 
    echo "$0 will add new packages from ${parent_project} to test_project"
    exit -1
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

for i in $(diff <( osc ls ${test_parent_project} ) <( osc ls ${parent_project} ) | grep ">" | cut -d" " -f2);do
    echo "Found new package $i in ${parent_project}. Creating a link to ${test_parent_project}"
    osc linkpac ${parent_project} $i ${test_parent_project}
done
    
