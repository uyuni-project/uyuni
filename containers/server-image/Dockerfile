# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest uyuni/server:4.4.0-1 uyuni/server:4.4.0-1.%RELEASE%

ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE

# TODO consider setting product repos from the configuration
ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh

# Main packages
RUN zypper ref
RUN zypper -n in vi
RUN zypper -n in python3-simplejson
RUN zypper --non-interactive install --auto-agree-with-licenses --force-resolution patterns-uyuni_server
RUN zypper mr -d repo-backports-update repo-sle-update

# Additional configuration
COPY configure.sh /usr/bin
RUN sh configure.sh

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="${PRODUCT} server container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} server"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="%PKG_VERSION%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

EXPOSE 443/tcp
EXPOSE 80/tcp
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME [ \
# DATA
"/var/lib/pgsql", "/var/spacewalk", "/var/cache", "/etc/salt", \
# salt
"/usr/share/susemanager/", \
"/usr/share/salt-formulas/", \
"/srv/susemanager/salt", \
"/srv/salt",  \
# home(s)
"/root/", \
"/home/", \
# Logs
"/var/log/rhn/", \
# configurations
"/etc/rhn", "/etc/pki/", "/etc/apache2/", "/etc/tomcat/"
]

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
