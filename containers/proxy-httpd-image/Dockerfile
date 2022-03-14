#!BuildTag: uyuni/proxy-httpd:latest uyuni/proxy-httpd:%PKG_VERSION% uyuni/proxy-httpd:%PKG_VERSION%.%RELEASE% uyuni/proxy-httpd:beta

ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE


ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="Proxy httpd container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to serve http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="Uyuni project"
LABEL org.opencontainers.image.url="https://www.uyuni-project.org/"
LABEL org.opencontainers.image.version="%%PKG_VERSION%%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="registry.opensuse.org/uyuni/proxy-httpd:%PKG_VERSION%.%RELEASE%"
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

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-PyYAML

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

COPY ssl.conf /etc/apache2/vhosts.d/ssl.conf

CMD uyuni-configure.py && /usr/sbin/start_apache2 -DFOREGROUND -k start
