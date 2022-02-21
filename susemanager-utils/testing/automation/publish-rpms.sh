#!/bin/bash

usage()
{
    echo "Usage: $0 -p project -r repo -a arch -d dir"
    echo "Where:"
    echo "  -p project is the project in the build service"
    echo "  -r repo is the repo in the build service"
    echo "  -a arch is the arch in the build service"
    echo "  -d dir is the directory where to create the repo"
    echo "  -q pkg is a specific package"
    echo "Example:"
    echo "$0 -p systemsmanagement:Uyuni:Master:PR:123 -r openSUSE_Leap_15.2 -a x86_64 -d /home/jenkins/jenkins-build/workspace/ -q 000product:Uyuni-Server-release -q 000product:Uyuni-Proxy-release"
    echo "This will create a repo in /home/jenkins/jenkins-build/workspace/systemsmanagement:Uyuni:Master:PR:123/openSUSE_Leap_15.2/x86_64"
}

packages=""

while getopts ":p:r:a:d:q:" opts;do
    case "${opts}" in
        p) echo "PPPP";obs_project=${OPTARG};;
        r) echo "RRRR";obs_repo=${OPTARG};;
        a) echo "AAA";obs_arch=${OPTARG};;
        d) echo "DDDD";repo_dir=${OPTARG};;
        q) packages="${packages} ${OPTARG}";;
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

# Checkout specific packages. For example, multibuild packages won't
# be downloaded by using "osc getbinaries PROJECT", so we need to
# be explicit. For example 000product:Uyuni-Server-release

for i in ${packages};do
    osc getbinaries $obs_project $i $obs_repo $obs_arch -d $repo_dir
done

cd $repo_dir
echo ${obs_repo} | grep "Ubuntu" -i
if [  ${?} -eq 0 ];then
    dpkg-scanpackages -m . /dev/null > Packages
    rm -f Packages.gz
    gzip Packages
else
     createrepo .
fi

echo "Publishing done."

