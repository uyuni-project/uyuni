#/bin/bash -e
echo "*************** CLEANING OBJECTS GENERATED INSIDE THE CONTAINER  ***************"
# Exit if the file does not exist or if it is empty
if [ ! -f /tmp/objects-init.txt -o -s /tmp/objects-init.txt ]; then
  echo "No file with the initial list of objects"
  exit 0
fi
find /manager > /tmp/objects-end.txt
while read OBJECTEND; do
  FOUND=FALSE
  while read OBJECTINIT; do
    if [ "${OBJECTINIT}" == "${OBJECTEND}" ]; then
      FOUND=TRUE
      break
    fi
  done < /tmp/objects-init.txt
  if [ "${FOUND}" == "FALSE" ]; then
    echo "${OBJECTEND}"
    if [ -d ${OBJECTEND} ]; then
        rm -rf ${OBJECTEND}
    else
	rm -f ${OBJECTEND}
    fi
  fi
done < /tmp/objects-end.txt
