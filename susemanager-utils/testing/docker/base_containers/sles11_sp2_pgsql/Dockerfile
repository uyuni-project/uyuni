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

# Create a tomcat user - this is required later by spacewalk-setup
RUN useradd tomcat

# spacewalk-setup requires the following binaries in this precise place (cheating with
# PATH does not help):
#
#   * /usr/sbin/spacewalk-service
#   * /usr/bin/rhn-config-schema.pl
#
# These binaries are alredy inside of the git checkout, we just need some
# symbolick links pointing to them. We do not have out git checkout mounted
# inside of the container at build time, hence we are going to cheat a little...

RUN mkdir -p /manager/spacewalk/admin/
RUN cd /manager/spacewalk/admin; touch spacewalk-servicel rhn-config-schema.pl
RUN ln -s /manager/spacewalk/admin/spacewalk-service /usr/sbin/
RUN ln -s /manager/spacewalk/admin/rhn-config-schema.pl /usr/bin/
RUN rm -rf /manager

# The symlinks are broken right now, but they will work fine when we mount our
# git checkout on /manager
