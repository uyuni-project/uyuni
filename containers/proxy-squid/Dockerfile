FROM registry.suse.com/suse/sle15:15.3

# Squid
EXPOSE 8080/tcp

VOLUME [ "/etc/uyuni", "/var/cache/squid" ]

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses squid

# TODO: remove, devel
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses vim less nmap

COPY proxy/installer/squid.conf /etc/squid/squid.conf

COPY containers/proxy-squid/uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && squid -z --foreground && squid -FC -d 1 --foreground
