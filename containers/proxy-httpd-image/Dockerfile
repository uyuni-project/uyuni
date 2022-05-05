#!BuildTag: uyuni/proxy-httpd:latest uyuni/proxy-httpd:%PKG_VERSION% uyuni/proxy-httpd:%PKG_VERSION%.%RELEASE%

ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE AS base


ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-rhnlib
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-broker

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-common
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-package-manager
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-redirect
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-ssl-cert-check
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-html
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses susemanager-tftpsync-recv     
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-PyYAML

# Additional material
COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

# Prepare for slimming down
COPY prepare_target.sh .
RUN sh prepare_target.sh

RUN ls -l /target/etc/sysconfig

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE AS slim

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3-PyYAML
COPY --from=base /target /



# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="${PRODUCT} proxy httpd container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="%PKG_VERSION%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-httpd:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

# http(s)
EXPOSE 80/tcp
EXPOSE 443/tcp

VOLUME ["/etc/uyuni", "/srv/tftpboot", "/var/cache/rhn"]



RUN sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES headers
RUN sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy_http

CMD uyuni-configure.py && /usr/sbin/start_apache2 -DFOREGROUND -k start
