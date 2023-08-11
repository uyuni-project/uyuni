#!/bin/bash -e

if [ $# -ne 2 ];
then
    echo "Usage: $0 <schema_name> <schema_version>"
    echo "where"
    echo "    schema_name        name of the schema to update (susemanager-schema or uyuni-reportdb-schema)"
    echo "    schema_version     version of the schema currently installed"
    exit 1
fi

if [[ "$1" == "susemanager-schema" ]];
then
    upgrade_dir="/etc/sysconfig/rhn/schema-upgrade/"
    additional_params=""
elif [[ "$1" == "uyuni-reportdb-schema" ]];
then
    upgrade_dir="/etc/sysconfig/rhn/reportdb-schema-upgrade/"
    additional_params="--reportdb"
else
    echo "Unknown schema $1. Use either susemanager-schema or uyuni-reportdb-schema."
    exit 1
fi

for i in $(find ${upgrade_dir} -name "$1-$2-to-*"); do
    echo $(basename $i)
    for j in $(find $i -name *.sql); do
        echo -e "\t$(basename $j)"; spacewalk-sql ${additional_params} $j | sed 's/^/\t\t/';
    done;
done
