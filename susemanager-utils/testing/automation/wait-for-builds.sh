#!/bin/bash

usage() {
  echo "Usage: $0 -a api -c config_file -p project -u"
  echo "project is mandatory. The rest are optionals."
  echo "u option is for unlocking the package after the build has finished"
}

api=https://api.opensuse.org
config_file=$HOME/.oscrc
lock="yes"
osc_timeout="2h"

while getopts ":a:c:p:u" o;do
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
        u)
            lock="no"
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

echo "DEBUG: api: $api ; config_file: $config_file ; project $project; lock: $lock"

if [ -z "${project}" ];then
    usage
    exit -1
fi 

echo "Waiting for $project to build"
# Sometimes the osc results -w gets stucked
# Autobuild has been informed but until we have a better solution,
# we are running it with a timeout command twice. If the second timeout
# is triggered, we exit with an error
timeout --foreground ${osc_timeout} osc -A ${api} -c ${config_file} results ${project} -w --xml
if [ $? -eq 124 ];then
    echo "Trying again to wait for ${project} to build"
    timeout --foreground ${osc_timeout} osc -A ${api} -c ${config_file} results ${project} -w --xml
    if [ $? -eq 124 ];then
        echo "Waiting for the results of ${project} got stucked twice"
        echo "Please check ${project} build results"
        echo "You can contact autobuild@suse.de if this does not get fixed"
        exit 124
    fi
fi

for i in $(osc -A $api -c $config_file ls $project);do
    if [ $lock == "yes" ];then
      echo "Locking $project/$i so there are not further rebuilds"
      osc -A $api -c $config_file lock $project $i
    fi  
done
