# SPDX-License-Identifier: MIT

#!BuildTag: uyuni/monitoring-prometheus:latest uyuni/monitoring-prometheus:%%prometheus_patch_version%%

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE

RUN set -euo pipefail && \
    zypper ref && zypper --non-interactive up && \
    zypper -n install --no-recommends \
        golang-github-prometheus-prometheus \
        curl && \
    zypper --non-interactive clean --all && \
    rpm -e container-suseconnect && \
    rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.prometheus
LABEL org.opencontainers.image.title="${PRODUCT} Prometheus"
LABEL org.opencontainers.image.name=monitoring-prometheus-image
LABEL org.opencontainers.image.description="Prometheus container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.2.1
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/monitoring-prometheus:%%prometheus_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT ["/usr/bin/prometheus"]
EXPOSE 9090
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9090/-/healthy"]

VOLUME /var/lib/prometheus

