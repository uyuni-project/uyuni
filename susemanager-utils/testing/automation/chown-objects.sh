#/bin/bash -e
if [ -z ${1} -o -z ${2} ]; then
  echo "This scripts needs two parameters: the first one for the UID, the second one for the GID"
  exit 1
fi
NEWUID=${1}
NEWGID=${2}
echo "*********** ADJUSTING PERMISSIONS FOR OBJECTS GENERATED INSIDE THE CONTAINER TO ${NEWUID}:${NEWGID} ***********"
# Exit if the file does not exist or if it is empty
if [ ! -f /tmp/objects-init.txt -o ! -s /tmp/objects-init.txt ]; then
  echo "No file with the initial list of objects"
  exit 0
fi
# remove known nodejs modules
pushd /manager
rm -rf ./web/html/src/node_modules
popd
find /manager | sort > /tmp/objects-end.txt
for LINE in $(diff /tmp/objects-init.txt /tmp/objects-end.txt|grep '^> .*$'|sed -e 's/^> //'); do
  case $LINE in
    /manager/web/html/src/node_modules/*) ;;
    *) chown ${NEWUID}:${NEWGID} ${LINE};;
  esac
done
