# Container used to test SUSE Manager Head
#
# VERSION                4.0.0

FROM opensuse/leap:15.0
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install the basic packages which are needed always
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

