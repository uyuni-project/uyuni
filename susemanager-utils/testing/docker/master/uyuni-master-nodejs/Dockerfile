# Container used to test the JavaScript code of Uyuni
#
# VERSION               4.2

FROM registry.mgr.suse.de/uyuni-master-base:latest
MAINTAINER Matei Albu "malbu@suse.de"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
