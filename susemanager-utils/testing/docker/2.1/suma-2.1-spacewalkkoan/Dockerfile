# Container used to test java code of SUSE Manager
#
# VERSION               0.1

FROM suma-2.1-base
MAINTAINER Flavio Castelli "fcastelli@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD initrd.gz /root/initrd.gz
ADD initrd.xz /root/initrd.xz

