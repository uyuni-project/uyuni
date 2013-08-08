# Container used to test java code of SUSE Manager
#
# VERSION               0.1

FROM sles11_sp2_base
MAINTAINER Flavio Castelli "fcastelli@suse.com"

# Make sure the package repository is up to date
RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install needed packages
RUN zypper in -y perl-DBD-Pg postgresql91-contrib postgresql91-server

ADD setup-db-postgres.sh /root/setup-db-postgres.sh
RUN sh /root/setup-db-postgres.sh
