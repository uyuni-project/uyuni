# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-postgres:latest

ARG BASE=registry.opensuse.org/opensuse/postgres:16
# or ARG BASE=registry.suse.com/suse/postgres:16
FROM $BASE

COPY uyuni-postgres-config.sh /docker-entrypoint-initdb.d/uyuni-postgres-config.sh
RUN chmod a+x /docker-entrypoint-initdb.d/uyuni-postgres-config.sh

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-postgres
LABEL org.opencontainers.image.name=server-postgres
LABEL org.opencontainers.image.title="${PRODUCT} Postgresql container"
LABEL org.opencontainers.image.description="${PRODUCT} Postgresql image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-postgres:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

