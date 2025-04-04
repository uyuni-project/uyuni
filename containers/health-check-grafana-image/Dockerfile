# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/health-check-grafana:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7

FROM $BASE
RUN zypper --non-interactive ref && zypper --non-interactive up && \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
        grafana \
        grafana-infinity-datasource

ENV GF_PATHS_PROVISIONING=/etc/grafana/provisioning
ENV GF_AUTH_ANONYMOUS_ENABLED=true
ENV GF_AUTH_ANONYMOUS_ORG_ROLE=Admin

ENV GF_PATHS_CONFIG=/etc/grafana/grafana.ini
ENV GF_PATHS_DATA=/var/lib/grafana
ENV GF_PATHS_HOME=/usr/share/grafana
ENV GF_PATHS_PLUGINS=/var/lib/grafana/plugins

EXPOSE 3000

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.health-check-grafana
LABEL org.opencontainers.image.name=health-check-grafana-image
LABEL org.opencontainers.image.title="${PRODUCT} health check Grafana container"
LABEL org.opencontainers.image.description="${PRODUCT} health check Grafana image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/health-check-grafana:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT ["grafana", "server", "--config", "/etc/grafana/grafana.ini"]
