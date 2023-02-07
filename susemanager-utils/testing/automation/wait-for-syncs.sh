#!/bin/bash +ex
tries=60
wait=10

function usage() {
    echo "Usage: ${0} -h host [-t tries] [-w wait]"
    echo "host: an IP or hostname (mandatory)"
    echo "tries: number of attempts before failing (Optional, default value is ${tries})"
    echo "wait: number of seconds to wait between retries (Optional, default value: ${wait})"
}

while getopts ":h:t:w" o; do
    case "${o}" in
        h)
            host=${OPTARG}
            ;;
        t)
            tries=${OPTARG}
            ;;
        w)
            wait=${OPTARG}
            ;;
        :)
            echo "ERROR: Option -${OPTARG} requires an argument"
            usage
            exit -1
            ;;
        \?)
            echo "ERROR: Invalid option -${OPTARG}"
            usage
            exit -2
            ;;
    esac
done
shift $((OPTIND-1))
if [ "${host}" == "" ]; then
    echo "ERROR: Please specify host"
    exit -3
fi

echo "Wait for last sync to finish"
i=0
echo "Waiting for the sync to end"
while [ ${i} -lt ${tries} ]; do
  wget -S http://${host}/sync-obs/sync-finished.successful 2>/dev/null
  if [ ${?} -eq 0 ];then
    echo "Previous sync finished successfully"
    rm -f sync-finished.successful
    break
  fi
  wget -S http://${host}/sync-obs/sync-finished.fail 2>/dev/null
  if [ ${?} -eq 0 ];then
    echo "Previous sync finished unsuccesfully"
    rm -f sync-finished.fail
    break
  fi
  i=$((${i}+1))
  sleep ${wait}
done
if [ ${i} -eq ${tries} ]; then
  echo "Reached max number of tries"
  exit -7
fi


echo "Triggering a sync...this can take several minutes"
curl http://${host}/cgi-bin/sync-obs.cgi


echo "Wait for sync to start and finish"
i=0
echo "Waiting for the sync to start"
while [ ${i} -lt ${tries} ]; do
  wget -S http://${host}/sync-obs/sync-started 2>/dev/null
  if [ ${?} -eq 0 ]; then
    echo "Sync started"
    break
  fi
  i=$((${i}+1))
  sleep ${wait}
done
if [ ${i} -eq ${tries} ]; then
  echo "Reached max number of tries"
  exit -4
fi

i=0
rm -f sync-started
echo "Waiting for the sync to end"
while [ ${i} -lt ${tries} ]; do
  wget -S http://${host}/sync-obs/sync-finished.successful 2>/dev/null
  if [ ${?} -eq 0 ];then
    echo "Sync finished successfully"
    break
  fi
  wget -S http://${host}/sync-obs/sync-finished.fail 2>/dev/null
  if [ ${?} -eq 0 ];then
    echo "Sync finished unsuccesfully"
    break
  fi
  i=$((${i}+1))
  sleep ${wait}
done
if [ ${i} -eq ${tries} ]; then
  echo "Reached max number of tries"
  exit -5
fi
if [ -f sync-finished.fail ];then
    rm -f sync-finished.fail
    exit -6
else
    rm -f sync-finished.successful
    exit 0
fi



