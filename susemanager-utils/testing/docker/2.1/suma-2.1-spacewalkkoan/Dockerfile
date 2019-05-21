# Container used to test java code of SUSE Manager
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/suma-2.1-base:1.0.0
LABEL maintainer="mc@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD initrd.gz /root/initrd.gz
ADD initrd.xz /root/initrd.xz

