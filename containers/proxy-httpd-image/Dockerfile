# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-httpd:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# The name of the package is controlled in the prjconf. This is just the fallback
ARG PXYRELEASENOTESPKG="release-notes-uyuni-proxy"

ARG PRODUCT_REPO

# Main packages
RUN [ -n "$PRODUCT_REPO" ] && zypper addrepo $PRODUCT_REPO product; \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses  \
        spacewalk-proxy-broker \
        spacewalk-proxy-redirect \
        spacewalk-proxy-html \
        ${PXYRELEASENOTESPKG} \
        python3-rhnlib \
        python3-PyYAML && \
    sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES headers && \
    sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy_http && \
    zypper --non-interactive clean --all && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Replace mod_log_config.conf so that the logger takes a special var HANDLER_TYPE
COPY mod_log_config.conf /etc/apache2/mod_log_config.conf

# Additional material
COPY --chmod=+x uyuni-configure.py /usr/bin/uyuni-configure.py

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="${PRODUCT} proxy httpd container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-httpd-image
LABEL org.opencontainers.image.version=5.2.2
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-httpd:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# http(s)
EXPOSE 80/tcp
EXPOSE 443/tcp

VOLUME ["/etc/uyuni", "/srv/tftpboot", "/var/cache/rhn"]

CMD uyuni-configure.py && /usr/sbin/start_apache2 -DFOREGROUND -k start
