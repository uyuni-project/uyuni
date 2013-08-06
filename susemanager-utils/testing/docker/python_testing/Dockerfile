# Container used to test python code of SUSE Manager
#
# VERSION               0.1

FROM sles11_sp2_base
# make sure the package repository is up to date
RUN zypper --non-interactive --gpg-auto-import-keys ref

RUN zypper in -y python-base python-gzipstream python-mock python-xml python-curl python-gpgme python-debian python-dmidecode python-logilab-common python-pam python-logilab-astng python-psycopg2 python python-unittest2 rpm-python python-iniparse python-urlgrabber python-gobject2 python-devel python-selinux python-setools python-crypto python-openssl python-ethtool python-newt yum make cx_Oracle
