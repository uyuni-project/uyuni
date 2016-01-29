# Container used to test cobbler against suse manager head
#
# VERSION               1.0.0

FROM suma-docker-registry.suse.de/sles12sp1:0.1.0
MAINTAINER Michael Calmer "mc@suse.com"


# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

# Install the packages required to run the cobbler tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Create a apache user - this is required later by cobbler tests
#RUN useradd apache


