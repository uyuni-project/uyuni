#! /bin/sh

. /etc/os-release

MAJ_VER=$(echo $VERSION_ID | awk -F. '{print $1}')

if [ $ID == "sles" -a $MAJ_VER -ge 12 ]; then
  echo "SLES"
elif [ $ID == "rhel" -a  $MAJ_VER -ge 8 ]; then
  echo "RHEL"
elif [ $ID == "rhel" ]; then
  echo "RHEL7"
fi
