# Container used to test SUSE Manager Head
#
# VERSION                4.0.0

FROM opensuse/leap:15.1
MAINTAINER Julio Gonzalez "jgonzalez@suse.com"

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install the packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Install yarn
ADD add_yarn.sh /root/add_yarn.sh
RUN /root/add_yarn.sh
