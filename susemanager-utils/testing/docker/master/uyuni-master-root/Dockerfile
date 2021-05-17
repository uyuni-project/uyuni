# Container used to test Uyuni
#
# VERSION                4.2

FROM opensuse/leap:15.3
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# When using SLES images better remove container-suseconnect
# We setup our own repositories
#RUN zypper rm -y container-suseconnect ||:

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install the basic packages which are needed always
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

