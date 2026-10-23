cd x86_64
wget -c https://download.opensuse.org/tumbleweed/repo/oss/x86_64/dmidecode-3.7-2.1.x86_64.rpm .
wget -c https://download.opensuse.org/tumbleweed/repo/oss/x86_64/librpmbuild10-4.20.1-11.1.x86_64.rpm .
cd -
createrepo .
