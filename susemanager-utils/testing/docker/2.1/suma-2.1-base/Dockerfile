# Container used to test SUSE Manager 2.1
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/sle11sp3
LABEL maintainer="mc@suse.com"


# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

# Install the packages required to run both the python and the Java tests
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh


# Create fake filesystem entries required to run spacewalk-setup
ADD create_fake_filesystem_entries.sh /root/create_fake_filesystem_entries.sh
RUN /root/create_fake_filesystem_entries.sh

# Create a tomcat user - this is required later by spacewalk-setup
RUN useradd tomcat

# Add other files required by spacewalk-setup
ADD ssh /root/.ssh
RUN chown -R root:root /root/.ssh
RUN chmod 600 /root/.ssh/*

ADD spacewalk-public.cert /usr/share/spacewalk/setup/spacewalk-public.cert

ADD gnupg /.gnupg
ADD webapp-keyring.gpg /etc/webapp-keyring.gpg
ADD susemanager-schema-2.1.50.5-0.7.1.noarch.rpm /root/susemanager-schema-2.1.50.5-0.7.1.noarch.rpm
ADD susemanager-schema-1.7.56.14-0.5.1.noarch.rpm /root/susemanager-schema-1.7.56.14-0.5.1.noarch.rpm

ENV PYTHONPATH /manager/client/rhel/rhnlib/:manager/suseRegisterInfo:/manager/client/rhel/rhn-client-tools/src
ENV PERLLIB /manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
ENV PATH /manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin


