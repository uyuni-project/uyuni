# Container used to test spacewalk-koan for Uyuni
#
# VERSION                4.0.0

FROM registry.mgr.suse.de/uyuni-master-base:latest
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD initrd.gz /root/initrd.gz
ADD initrd.xz /root/initrd.xz

