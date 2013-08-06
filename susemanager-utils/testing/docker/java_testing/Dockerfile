# Container used to test java code of SUSE Manager
#
# VERSION               0.2

FROM sles11_sp2_base
MAINTAINER Johannes Renner "jrenner@suse.de"

# Make sure the package repository is up to date
RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install needed packages
RUN zypper in -y ant ant-junit java-1_6_0-ibm-devel make postgresql-server python spacewalk-setup sudo

