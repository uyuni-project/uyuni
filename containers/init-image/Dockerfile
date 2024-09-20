# Specify the license of the container build description (see also the LICENSE file)
# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/init:latest

ARG INIT_BASE=opensuse/leap:15.6
FROM $INIT_BASE

# Create stable static UID and GID for salt, tomcat, apache (wwwrun), postgres, ...
# postgresql UID/GID 999 to be compatible with upstream
RUN groupadd -r --gid 10550 susemanager && \
  groupadd -r --gid 10551 tomcat && \
  groupadd -r --gid 10552 www && \
  groupadd -r --gid 10553 wwwrun && \
  groupadd -r --gid 10554 salt && \
  groupadd -r --gid 10555 tftp && \
  groupadd -r --gid 999 postgres

RUN useradd -r -s /usr/sbin/nologin -G susemanager,www -g tomcat -d /usr/share/tomcat --uid 10551 tomcat && \
  useradd -r -s /usr/sbin/nologin -G susemanager,www -g wwwrun -d /var/lib/wwwrun --uid 10552 wwwrun && \
  useradd -r -s /usr/sbin/nologin -G susemanager -g salt -d /var/lib/salt --uid 10554 salt && \
  useradd -r -s /usr/sbin/nologin -g tftp -d /srv/tftpboot --uid 10555 tftp && \
  useradd -r -s /usr/bin/bash -g postgres -d /var/lib/pgsql --uid 999 postgres

# Fill the image with content and clean the cache(s)
RUN set -euo pipefail; zypper -n in --no-recommends systemd gzip; zypper -n clean; rm -rf /var/log/*
CMD ["/usr/lib/systemd/systemd"]

RUN mkdir -p /etc/systemd/system.conf.d/ && \
    printf "[Manager]\nLogColor=no" > \
        /etc/systemd/system.conf.d/01-nocolor.conf

# disabled as recommended by postgresql until we need to run systemd in container
# https://wiki.postgresql.org/wiki/Systemd
RUN mkdir -p /etc/systemd/logind.conf.d/ && \
    printf "[Login]\nRemoveIPC=no" > \
        /etc/systemd/logind.conf.d/disable-removeipc.conf

RUN systemctl disable getty@tty1.service

RUN ((rpm -qa | grep skelcd-EULA-bci) && zypper -n rm skelcd-EULA-bci) || true

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.init
LABEL org.opencontainers.image.name=init-image
LABEL org.opencontainers.image.title="${PRODUCT} Init Image"
LABEL org.opencontainers.image.description="This image initialize users, groups and software for ${PRODUCT} Container"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/init:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix

HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
