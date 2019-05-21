# Container used to test SUSE Manager 3.2
#
# VERSION               1.1.0

FROM registry.mgr.suse.de/sles12sp3:latest
LABEL maintainer="mc@suse.com"


# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

# Install the packages required to run both the python and the Java tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Add other files required by spacewalk-setup
ADD ssh /root/.ssh
RUN chown -R root:root /root/.ssh
RUN chmod 600 /root/.ssh/*

ENV PYTHONPATH /gatherer/

