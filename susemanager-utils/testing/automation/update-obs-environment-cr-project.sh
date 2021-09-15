#!/bin/bash

parent_project="systemsmanagement:Uyuni:Master"

if [ $# -ne 1 ];then
    echo "usage: $0 N"
    echo "where N is the environment"
    echo "$0 will add new packages in ${parent_project} to ${parent_project}:N:CR"
    exit -1
fi    

test_parent_project="${parent_project}:TEST:${1}:CR"

for i in $(diff <( osc ls ${test_parent_project} ) <( osc ls ${parent_project} ) | grep ">" | cut -d" " -f2);do
    echo "Found new package $i in ${parent_project}. Creating a link to ${test_parent_project}"
    osc linkpac ${parent_project} $i ${test_parent_project}
done
    
