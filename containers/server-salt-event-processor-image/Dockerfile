# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-salt-event-processor:latest

ARG BASE=registry.suse.com/bci/bci-base:15.6
FROM $BASE

# Install main packages
RUN zypper ref && zypper --non-interactive install \
    spacewalk-java-lib \
    procps

# Add all your files (including modified rhn.jar)
ADD --chown=root:root root.tar.gz /

# Labels
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-salt-event-processor
LABEL org.opencontainers.image.name=server-salt-event-processor-image
LABEL org.opencontainers.image.title="${PRODUCT} Salt Event Processor container"
LABEL org.opencontainers.image.description="${PRODUCT} Salt Event Processor microservice"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.6
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-salt-event-processor:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD ["pgrep", "-f", "com.suse.saltevent.SaltEventProcessor"]

CMD ["/usr/sbin/mgr_salt_event_processor"]