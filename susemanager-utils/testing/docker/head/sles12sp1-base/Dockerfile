# Container used to test SUSE Manager 1.7
#
# VERSION               0.1

#FROM sles12sp1
FROM suma-docker-registry.suse.de/sles12sp1
MAINTAINER Flavio Castelli "fcastelli@suse.com"

# remove packages not needed
ADD remove_packages.sh /root/remove_packages.sh
RUN /root/remove_packages.sh

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

