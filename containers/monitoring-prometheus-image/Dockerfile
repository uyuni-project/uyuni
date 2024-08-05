# SPDX-License-Identifier: MIT

#     Copyright (c) 2024 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/prometheus:%%prometheus_patch_version%%
#!BuildTag: uyuni/prometheus:latest

ARG BASE=bci/bci-base:15.6
FROM $BASE

MAINTAINER openSUSE (https://www.opensuse.org/)

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.prometheus
LABEL org.opencontainers.image.title="${PRODUCT} Prometheus"
LABEL org.opencontainers.image.name="monitoring-prometheus-image"
LABEL org.opencontainers.image.description="Prometheus container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.0.8
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/prometheus:%%prometheus_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix

RUN set -euo pipefail; zypper -n in --no-recommends golang-github-prometheus-prometheus curl; zypper -n clean; rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}
ENTRYPOINT ["/usr/bin/prometheus"]
EXPOSE 9090
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9090/-/healthy"]

VOLUME /var/lib/prometheus

