# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest uyuni/server:%PKG_VERSION% uyuni/server:%PKG_VERSION%.%RELEASE%

ARG INIT_BASE=registry.suse.com/bci/bci-base:15.4
FROM $INIT_BASE

ARG PRODUCT_REPO
ARG PRODUCT_PATTERN="patterns-uyuni_server"

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Copy Uyuni setup script
COPY setup_env.sh /root/setup_env.sh
COPY uyuni-setup.service /usr/lib/systemd/system/

# Main packages
COPY remove_unused.sh .  
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf
# TODO Remove vi and possibly python3-simplejson
RUN zypper ref
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    ${PRODUCT_PATTERN} \
    javamail \
    libyui-ncurses-pkg16 \
    vim \
    python3-simplejson

RUN sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb

RUN systemctl enable uyuni-setup

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
# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp
# PXE server
EXPOSE 69/udp
# Cobbler
EXPOSE 25151/tcp
# Postgres for report db
EXPOSE 5432/tcp
# HTTP ssh tunnel
#EXPOSE 1232/tcp
# HTTPS ssh tunnel
#EXPOSE 1233/tcp
#TODO these ports should be exposed in debug mode
# Tomcat remote debug
#EXPOSE 8000/tcp
# Taskomatic remote debug
#EXPOSE 8001/tcp

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
