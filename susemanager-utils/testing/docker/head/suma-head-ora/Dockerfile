# Container used to test java and python code of SUSE Manager against Oracle database
#
# VERSION               1.0.0

FROM suma-docker-registry.mgr.suse.de/suma-head-base:1.0.1
MAINTAINER Flavio Castelli "fcastelli@suse.com"

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

ADD tnsnames.ora /etc/tnsnames.ora
ADD rhn.conf /etc/rhn/rhn.conf
ADD rhn.conf /root/rhn.conf
