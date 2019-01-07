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
              python-dateutil \
              python-debian \
              python-dmidecode \
              python-enum34 \
              python-ethtool \
              python-gobject2 \
              python-gpgme \
              python-gzipstream \
              python-iniparse \
              python-newt \
              python-pam \
              python-psycopg2 \
              python-pyOpenSSL \
              python-pycrypto \
              python-pycurl \
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
             java-1_8_0-openjdk-devel \
             pam \
             sudo \
             tar

