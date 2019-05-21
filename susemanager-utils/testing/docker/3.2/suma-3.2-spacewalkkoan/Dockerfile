# Container used to test java code of SUSE Manager
#
# VERSION               3.2.0

FROM registry.mgr.suse.de/suma-3.2-base:latest
LABEL maintainer="mc@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD initrd.gz /root/initrd.gz
ADD initrd.xz /root/initrd.xz

