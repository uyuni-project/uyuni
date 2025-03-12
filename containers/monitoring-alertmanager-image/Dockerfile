# SPDX-License-Identifier: MIT

#     Copyright (c) 2025 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/monitoring-alertmanager:latest uyuni/monitoring-alertmanager:%%alertmanager_patch_version%%

ARG BASE=registry.suse.com/bci/bci-base:15.6
FROM $BASE

RUN set -euo pipefail && \
    zypper -n install --no-recommends \
        golang-github-prometheus-alertmanager \
        curl && \
    zypper -n clean && \
    rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.alertmanager
LABEL org.opencontainers.image.name=monitoring-alertmanager-image
LABEL org.opencontainers.image.title="${PRODUCT} Alertmanager"
LABEL org.opencontainers.image.description="Alertmanager container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/monitoring-alertmanager:%%alertmanager_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT ["/usr/bin/prometheus-alertmanager"]
EXPOSE 9093
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9093/-/healthy"]

VOLUME /var/lib/prometheus/alertmanager

