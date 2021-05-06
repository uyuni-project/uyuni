#!/bin/bash

usage()
{
    echo "Usage: $0 -p project -r repo -a arch -d dir"
    echo "Where:"
    echo "  -p project is the project in the build service"
    echo "  -r repo is the repo in the build service"
    echo "  -a arch is the arch in the build service"
    echo "  -d dir is the directory where to create the repo"
    echo "Example:"
    echo "$0 -p systemsmanagement:Uyuni:Master:PR:123 -r openSUSE_Leap_15.2 -a x86_64 -d /home/jenkins/jenkins-build/workspace/"
    echo "This will create a repo in /home/jenkins/jenkins-build/workspace/systemsmanagement:Uyuni:Master:PR:123/openSUSE_Leap_15.2/x86_64"
}

while getopts ":p:r:a:d:" opts;do
    case "${opts}" in
        p) obs_project=${OPTARG};;
        r) obs_repo=${OPTARG};;
        a) obs_arch=${OPTARG};;
        d) repo_dir=${OPTARG};;
        \?) usage;exit -1;;
    esac
done
shift $((OPTIND-1))

if [ -z "${obs_project}" ] || \
   [ -z "${obs_repo}" ] || \
   [ -z "${obs_arch}" ] || \
   [ -z "${repo_dir}" ];then
    usage
    echo "p ${obs_project} r ${obs_repo} a ${obs_arch} d ${repo_dir}"
    exit -1
fi

repo_dir=$repo_dir/$obs_project/$obs_repo/$obs_arch

osc getbinaries $obs_project $obs_repo $obs_arch -d $repo_dir
cd $repo_dir && createrepo .
