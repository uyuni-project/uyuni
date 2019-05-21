# Container used to test java code of SUSE Manager
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/suma-2.1-base:1.0.0
LABEL maintainer="mc@suse.com"

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

ADD tnsnames.ora /etc/tnsnames.ora
ADD rhn.conf /etc/rhn/rhn.conf
ADD rhn.conf /root/rhn.conf
