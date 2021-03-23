#!/bin/bash

usage() {
  echo "Usage: $0 -a api -c config_file -p project"
  echo "project is mandatory. The rest are optionals."
}

api=https://api.opensuse.org
config_file=$HOME/.oscrc

while getopts ":a:c:p:" o;do
    case "${o}" in
        a)
            api=${OPTARG}
            ;;
        c)
            config_file=${OPTARG}
            ;;
        p)
            project=${OPTARG}
            ;;
        :)
            echo "ERROR: Option -$OPTARG requires an argument"
            ;;
        \?)
            echo "ERROR: Invalid option -$OPTARG"
            usage
            ;;
    esac
done
shift $((OPTIND-1))

echo "DEBUG: api: $api ; config_file: $config_file ; project $project"

if [ -z "${project}" ];then
    usage
    exit -1
fi 

for i in $(osc -A $api -c $config_file ls $project);do
    echo "Checking $project/$i"
    osc -A $api -c $config_file results $project $i -w
done
