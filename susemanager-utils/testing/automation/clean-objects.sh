#/bin/bash -e
echo "*************** CLEANING OBJECTS GENERATED INSIDE THE CONTAINER  ***************"
# Exit if the file does not exist or if it is empty
if [ ! -f /tmp/objects-init.txt -o ! -s /tmp/objects-init.txt ]; then
  echo "No file with the initial list of objects"
  exit 0
fi
find /manager | sort > /tmp/objects-end.txt
for LINE in $(diff /tmp/objects-init.txt /tmp/objects-end.txt|grep '^> .*$'|sed -e 's/^> //'); do
  if [ -d ${LINE} ]; then
    rm -rf ${LINE}
  else
    rm -f ${LINE}
  fi
done
