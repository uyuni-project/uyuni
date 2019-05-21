# Container used to test SUSE Manager HEAD
#
# VERSION               3.1.0

FROM registry.mgr.suse.de/sles12sp2:latest
LABEL maintainer="mc@suse.com"

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

# Install the packages required to run both the python and the Java tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# add schema RPMs for migration tests
ADD susemanager-schema-2.1.50.5-0.7.1.noarch.rpm /root/susemanager-schema-2.1.50.5-0.7.1.noarch.rpm
ADD susemanager-schema-3.0.10-1.1.noarch.rpm /root/susemanager-schema-3.0.10-1.1.noarch.rpm
ADD susemanager-schema-3.1.6-1.1.noarch.rpm /root/susemanager-schema-3.1.6-1.1.noarch.rpm

# Create fake filesystem entries required to run spacewalk-setup
ADD create_fake_filesystem_entries.sh /root/create_fake_filesystem_entries.sh
RUN /root/create_fake_filesystem_entries.sh

# Create a tomcat user - this is required later by spacewalk-setup
RUN useradd tomcat

# Add other files required by spacewalk-setup
ADD ssh /root/.ssh
RUN chown -R root:root /root/.ssh
RUN chmod 600 /root/.ssh/*

#ADD spacewalk-public.cert /usr/share/spacewalk/setup/spacewalk-public.cert

ADD gnupg /.gnupg
ADD webapp-keyring.gpg /etc/webapp-keyring.gpg

ENV PYTHONPATH /manager/client/rhel/rhnlib/:/manager/client/rhel/rhn-client-tools/src
ENV PERLLIB /manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
ENV PATH /manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin


