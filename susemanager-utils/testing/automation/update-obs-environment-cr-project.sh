#!/bin/bash

if [ $# -ne 1 ];then
    echo "usage: $0 N"
    echo "where N is the environment"
    echo "$0 will add new packages in systemsmanagement:Uyuni:Master to systemsmanagement:Uyuni:Master:TEST:N:CR"
    exit -1
fi    


for i in $(diff <( osc ls systemsmanagement:Uyuni:Master:TEST:$1:CR ) <( osc ls systemsmanagement:Uyuni:Master ) | grep ">" | cut -d" " -f2);do
    echo "Found new package $i in systemsmanagement:Uyuni:Master. Creating a link to systemsmanagement:Uyuni:Master:TEST:$1:CR"
    osc linkpac systemsmanagement:Uyuni:Master $i systemsmanagement:Uyuni:Master:TEST:$1:CR
done
    
