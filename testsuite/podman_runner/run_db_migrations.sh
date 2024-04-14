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
    upgrade_dir="/usr/share/susemanager/db/schema-upgrade/"
    additional_params=""
elif [[ "$1" == "uyuni-reportdb-schema" ]];
then
    upgrade_dir="/usr/share/susemanager/db/reportdb-schema-upgrade/"
    additional_params="--reportdb"
else
    echo "Unknown schema $1. Use either susemanager-schema or uyuni-reportdb-schema."
    exit 1
fi

# Start with the given schema name and version
schema="$1"
version="$2"

# Get the upgrade dir for the current schema and version
current_dir="$(find ${upgrade_dir} -name "$schema-$version-to-*")"
until [ -z "${current_dir}" ]
do
    basename "${current_dir}"

    # Apply all the sql scripts from this directory
    while IFS= read -r -d '' file
    do
        echo -e "\t$(basename "${file}")"; spacewalk-sql ${additional_params} "${file}" | sed 's/^/\t\t/';
    done < <(find "${current_dir}" -name '*.sql' -print0 | sort -z)

    # Set the next schema and version from the directory name
    schema=$(basename "${current_dir}" | sed -e 's/.*-to-\([a-z-]\+\)-.*$/\1/')
    version=$(basename "${current_dir}" | sed -e 's/.*-\([0-9.]\+\)$/\1/')

    # Check if another upgrade directory exists
    current_dir=$(find ${upgrade_dir} -name "$schema-$version-to-*")
done
