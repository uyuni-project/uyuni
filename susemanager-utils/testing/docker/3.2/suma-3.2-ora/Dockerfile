# Container used to test java code of SUSE Manager
#
# VERSION               1.1.1

FROM registry.mgr.suse.de/suma-3.2-base:latest
MAINTAINER Michael Calmer "mc@suse.de"

# Add the repositories
#ADD add_repositories.sh /root/add_repositories.sh
#RUN /root/add_repositories.sh

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

ADD tnsnames.ora /etc/tnsnames.ora
ADD rhn.conf /etc/rhn/rhn.conf
ADD rhn.conf /root/rhn.conf

RUN groupadd dba

RUN useradd oracle --gid dba --create-home

ADD db-download /root/db-download
RUN /root/db-download

# requires privileges
#ADD db-setup /root/db-setup
#RUN /root/db-setup

