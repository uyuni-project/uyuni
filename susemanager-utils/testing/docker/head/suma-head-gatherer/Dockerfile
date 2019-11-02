# Container used to test SUSE Manager Head
#
# VERSION               4.0.0

FROM registry.mgr.suse.de/suma-head-root:latest
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# Install the packages required to run both the python and the Java tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Add other files required by spacewalk-setup
ADD ssh /root/.ssh
RUN chown -R root:root /root/.ssh
RUN chmod 600 /root/.ssh/*

ENV PYTHONPATH /gatherer/

