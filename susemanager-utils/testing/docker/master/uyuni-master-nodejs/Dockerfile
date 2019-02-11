# Container used to test the JavaScript code of SUSE Manager
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/uyuni-master-base:latest
MAINTAINER Matei Albu "malbu@suse.de"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
