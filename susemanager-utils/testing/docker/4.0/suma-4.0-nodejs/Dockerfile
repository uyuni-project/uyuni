# Container used to test the JavaScript code of SUSE Manager
#
# VERSION               4.0.0

FROM registry.mgr.suse.de/suma-4.0-base:latest
LABEL maintainer="malbu@suse.de"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
