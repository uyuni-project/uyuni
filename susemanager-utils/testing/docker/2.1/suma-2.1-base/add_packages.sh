#!/bin/bash
set -e

# make sure the package repository is up to date
zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run spacewalk-setup inside of the container
zypper in -y perl \
             perl-Params-Validate \
             perl-Mail-RFC822-Address \
             perl-XML-LibXML \
             perl-XML-SAX \
             perl-DateTime \
             perl-Frontier-RPC \
             perl-libwww-perl \
             perl-Net-LibIDN \
             perl-Satcon \
             perl-DBI

# Packages required to run the python unit tests
zypper in -y  cx_Oracle \
              make \
              python \
              python-argparse \
              python-base \
              python-configobj \
              python-crypto \
              python-curl \
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
              python-logilab-astng \
              python-logilab-common \
              python-mock \
              python-newt \
              python-nose \
              python-openssl \
              python-pam \
              python-psycopg2 \
              python-selinux \
              python-setools \
              python-unittest2 \
              python-urlgrabber \
              python-xml \
              rpm-python \
              yum

# Pylint packages - for some reason 2.1 wants this specific version of pylint (and its dependencies)
zypper in -y --oldpackage pylint-0.15.0-1.17 \
                          python-logilab-common-0.35.0-1.17 \
                          python-logilab-astng-0.17.3-1.17

# Packages required to run the python unit tests
zypper in -y ant \
             ant-junit \
             apache-ivy \
             java-1_7_0-ibm-devel \
             sudo \
             tar
