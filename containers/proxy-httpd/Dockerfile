#!BuildTag: proxy-httpd:latest proxy-httpd:%PKG_VERSION% proxy-httpd:%PKG_VERSION%.%RELEASE%

# HACK: Open Build Service currently only supports short names. Assume short name by default, allow to override to qualified name via ARG
ARG BASE_IMAGE_PREFIX=
FROM ${BASE_IMAGE_PREFIX}suse/sle15:15.3

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-PyYAML

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="Proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to serve http requests"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-httpd:%PKG_VERSION%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
# endlabelprefix

# http(s)
EXPOSE 80/tcp
EXPOSE 443/tcp

VOLUME ["/etc/uyuni", "/srv/tftpboot", "/var/cache/rhn"]

# TODO: remove, devel
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses vim less

# TODO: check and adjust
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
 python3-rhnlib spacewalk-proxy-broker \
 spacewalk-proxy-common spacewalk-proxy-package-manager \
 spacewalk-proxy-redirect spacewalk-ssl-cert-check

COPY containers/proxy-httpd/uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

COPY containers/proxy-httpd/ssl.conf /etc/apache2/vhosts.d/ssl.conf

CMD uyuni-configure.py && /usr/sbin/start_apache2 -DFOREGROUND -k start
