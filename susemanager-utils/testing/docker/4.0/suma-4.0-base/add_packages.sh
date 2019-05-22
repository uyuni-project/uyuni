#!/bin/bash
set -e

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
              python3-dateutil \
              python3-debian \
              python3-dmidecode \
              python3-ethtool \
              python3-gobject2 \
              python3-gpgme \
              python3-gzipstream \
              python3-iniparse \
              python3-newt \
              python3-python-pam \
              python3-psycopg2 \
              python3-pyOpenSSL \
              python3-pycrypto \
              python3-pycurl \
              python3-selinux \
              python3-simplejson \
              python3-urlgrabber \
              python3-xml \
              python3-rpm \
              yum

# Packages required to run the Java unit tests
zypper --non-interactive in ant \
             ant-junit \
             apache-ivy \
             java-11-openjdk-devel \
             pam \
             sudo \
             tar
