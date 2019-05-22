# Container used to test SUSE Manager 4.0
#
# VERSION                4.0.0

FROM registry.mgr.suse.de/sles15sp1:latest
LABEL maintainer="Michael.Calmer@suse.com"

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install the basic packages which are needed always
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

