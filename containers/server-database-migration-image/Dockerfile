# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-database-migration:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE

# Create stable static UID and GID for postgres, ...
# postgresql UID/GID 999 to be compatible with upstream
RUN groupadd -r --gid 999 postgres
RUN useradd -r -s /usr/bin/bash -g postgres -d /var/lib/pgsql --uid 999 postgres

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
      postgresql18-server \
      postgresql18-contrib \
      postgresql16-server \
      postgresql16-contrib \
      postgresql14-server \
      postgresql14-contrib && \
    zypper --non-interactive clean --all && \
    rpm -e zypper libzypp container-suseconnect && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-database-migration
LABEL org.opencontainers.image.name=server-database-migration-image
LABEL org.opencontainers.image.title="${PRODUCT} database migration container"
LABEL org.opencontainers.image.description="${PRODUCT} database migration image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-database-migration:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"
