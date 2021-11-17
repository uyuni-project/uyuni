FROM registry.suse.com/suse/sle15:15.3

# Squid
EXPOSE 8080/tcp

VOLUME "/etc/uyuni"

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product


# TODO: check and adjust

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses squid

CMD /usr/sbin/squid -FC --foreground
