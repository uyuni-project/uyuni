# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-postgresql:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
ARG PG_BASE=registry.opensuse.org/opensuse/postgres:18

FROM $BASE AS builder
RUN zypper --non-interactive install --download-only --force --no-recommends postgresql18-contrib tar && \
    mkdir -p /opt/contrib /opt/results && \
    find /var/cache/zypp/packages \( -name postgresql18-contrib* -o -name tar* \) -exec cp {} /opt/contrib \; && \
    cd /opt/contrib && rpm2cpio postgresql18-contrib.rpm | cpio -idmv && rpm2cpio tar*.rpm | cpio -idmv && \
    find . -name pg_trgm* -type f -exec cp --parents {} ../results/ \; && \
    cp --parents /usr/bin/tar ../results/

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
LABEL org.opencontainers.image.version=5.2.3
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
