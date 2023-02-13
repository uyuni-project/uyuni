# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest uyuni/server:4.4.0-1 uyuni/server:4.4.0-1.%RELEASE%

ARG INIT_BASE=registry.suse.com/bci/bci-base:15.4
FROM $INIT_BASE

ARG PRODUCT_REPO
ARG PRODUCT_PATTERN="patterns-uyuni_server"

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Copy Uyuni setup script
COPY setup_env.sh /root/setup_env.sh

# Main packages
# TODO Remove vi and possibly python3-simplejson
# snapper is installed by the setupscript anyway... pre-install it
RUN zypper ref
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses  \
    ${PRODUCT_PATTERN} \
    snapper \
    btrfsprogs \
    libyui-ncurses-pkg16 \
    vim


# FIXME hack to correct the report db script to work on containers
RUN cp /usr/bin/uyuni-setup-reportdb /usr/bin/uyuni-setup-reportdb.original
RUN sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb
RUN sed -i 's/-a "$PG_SOCKET"//g' /usr/bin/uyuni-setup-reportdb

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="${PRODUCT} server container"
LABEL org.opencontainers.image.description="All-in-one ${PRODUCT} server image"
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
"/etc/rhn", "/etc/pki/", "/etc/apache2/", "/etc/tomcat/" \
]

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
