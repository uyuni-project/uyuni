FROM registry.suse.com/suse/sle15:15.3

# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME "/etc/uyuni"

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-salt

# TODO -> remove the following block of lines after the merge
COPY proxy/proxy/salt-broker/salt-broker /usr/bin/salt-broker
RUN chmod +x /usr/bin/salt-broker
COPY proxy/proxy/salt-broker/broker /etc/salt/broker

COPY containers/proxy-salt-broker/uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && salt-broker
