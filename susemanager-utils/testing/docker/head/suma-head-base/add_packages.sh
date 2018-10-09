#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# this package has a file conflict with python-base
# and this package is not on SLES12 SP1
# a lock prevent the installation
zypper addlock python-strict-tls-check

# Packages required to run spacewalk-setup inside of the container
zypper --non-interactive in perl \
             perl-Params-Validate \
             perl-Mail-RFC822-Address \
             perl-XML-LibXML \
             perl-XML-SAX \
             perl-DateTime \
             perl-Frontier-RPC \
             perl-libwww-perl \
             perl-Net-LibIDN \
             perl-Satcon \
             perl-Term-Size \
             perl-Term-Completion \
             perl-DBI \
             which \
             timezone \
             aaa_base \
             net-tools

# Packages required to run the python unit tests
zypper --non-interactive in  \
              make \
              python \
              python-argparse \
              python-base \
              python-configobj \
              python-dateutil \
              python-debian \
              python-devel \
              python-dmidecode \
              python-enum34 \
              python-ethtool \
              python-gobject2 \
              python-gpgme \
              python-gzipstream \
              python-iniparse \
              python-mock \
              python-newt \
              python-nose \
              python-pam \
              python-psycopg2 \
              python-pyOpenSSL \
              python-pycrypto \
              python-pycurl \
              python-pylint \
              python-selinux \
              python-setools \
              python-simplejson \
              python-urlgrabber \
              python-xml \
              rpm-python \
              yum

# Packages required to run the Java unit tests
zypper --non-interactive in ant \
             ant-junit \
             apache-ivy \
             java-1_8_0-ibm-devel \
             pam-modules \
             sudo \
             tar

# Packages for easier debugging
zypper --non-interactive in vim less

