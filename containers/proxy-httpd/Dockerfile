FROM suse/sle15:15.3

# http(s)
EXPOSE 80/tcp
EXPOSE 443/tcp

VOLUME ["/etc/uyuni", "/srv/tftpboot", "/var/cache/rhn"]

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-PyYAML

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# TODO: remove, devel
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses vim less

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
 python3-rhnlib spacewalk-proxy-broker \
 spacewalk-proxy-common spacewalk-proxy-package-manager \
 spacewalk-proxy-redirect spacewalk-ssl-cert-check

COPY containers/proxy-httpd/uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

COPY containers/proxy-httpd/ssl.conf /etc/apache2/vhosts.d/ssl.conf

CMD uyuni-configure.py && /usr/sbin/start_apache2 -DFOREGROUND -k start
