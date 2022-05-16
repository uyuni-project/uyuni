#!/bin/bash

usage()
{
    echo "Usage: ${0} -u user -p pull"
    echo "Where:"
    echo "  -u user email that started the test"
    echo "  -p pull request number"
}

while getopts "u:p:" opts;do
    case "${opts}" in
        u) user_email=${OPTARG};;
        p) pull_request_number=${OPTARG};;
        \?)usage;exit -1;;
    esac
done
shift $((OPTIND-1))
if [ -z "${user_email}" ] || \
   [ -z "${pull_request_number}" ];then
     usage
     echo "user email :${user_email}"
     echo "pull request numbeR: ${pull_request_number}"
     exit -1
fi

echo "DEBUG: removing previous environment for user: ${user_email} and PR ${pull_request_number}"
lockfiles=$(grep -H PR:${pull_request_number} /tmp/env-suma-pr-*.lock.info | cut -d: -f1 | xargs grep -H user:${email_to} | cut -d: -f1 | xargs grep -H keep: | cut -d: -f1 | rev | cut -d. -f1 --complement | rev)
if [ "${lockfiles}" != "" ];then
    echo "DEBUG: found lockfiles ${lockfiles}"
    echo "DEBUG: remove job"
    echo ${lockfiles} | xargs -I ARG grep -H ARG /var/spool/atjobs/* | cut -d: -f1 |xargs rm -fv
    echo "DEBUG: remove files"
    echo ${lockfiles} | xargs rm -vf
    echo ${lockfiles} | xargs -I ARG rm -vf ARG.info
else
  echo "DEBUG: No lockfiles found"
  echo "DEBUG: Nothing to do"
fi

