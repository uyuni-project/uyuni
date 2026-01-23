# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-postgresql:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
ARG PG_BASE=registry.opensuse.org/opensuse/postgres:18

FROM $BASE AS builder
RUN zypper --non-interactive install --download-only postgresql18-contrib && \
    mkdir -p /opt/contrib /opt/results && \
    find /var/cache/zypp/packages -name postgresql18-contrib* -exec cp {} /opt/contrib \; && \
    cd /opt/contrib && rpm2cpio postgresql18-contrib.rpm | cpio -idmv && \
    find . -name pg_trgm* -exec cp --parents {} ../results/ \;

FROM $PG_BASE
COPY --from=builder /opt/results/ /
ADD --chown=root:root root.tar.gz /

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-postgresql
LABEL org.opencontainers.image.name=server-postgresql-image
LABEL org.opencontainers.image.title="${PRODUCT} PostgreSQL container image"
LABEL org.opencontainers.image.description="${PRODUCT} PostgreSQL container image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.2.2
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-postgresql:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# Hard code the postgres UID / GID to the upstream image value and what was in the big container
# The files are chown'ed in the entrypoint
RUN /usr/sbin/groupmod -g 999 postgres && \
    /usr/sbin/usermod -u 999 -g 999 postgres

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["postgres"]
