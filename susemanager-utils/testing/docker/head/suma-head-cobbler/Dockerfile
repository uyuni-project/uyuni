# Container used to test cobbler against suse manager head
#
# VERSION               4.0.0

FROM registry.mgr.suse.de/suma-head-root:latest
LABEL maintainer="Michael.Calmer@suse.com"

# Install the packages required to run the cobbler tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Create fake filesystem entries required to run cobber tests
ADD create_fake_filesystem_entries.sh /root/create_fake_filesystem_entries.sh
RUN /root/create_fake_filesystem_entries.sh

# Create a apache user - this is required later by cobbler tests
RUN useradd apache

# Add files required to run the unit tests
ADD fake-template /tmp/cobblertest/fake-template
#apache conf.d
ADD cobbler-apache.conf /root/cobbler-apache.conf
#cobbler modules
ADD modules.conf /root/modules.conf
ADD sample.ks /root/sample.ks
#sysconfog
ADD apache2 /root/apache2
#apache vhost.d
ADD cobbler_web.conf /root/cobbler_web.conf
