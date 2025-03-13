# SPDX-License-Identifier: MIT

#     Copyright (c) 2025 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/monitoring-grafana:latest uyuni/monitoring-grafana:%%grafana_patch_version%%

ARG BASE=registry.suse.com/bci/bci-base:15.6
FROM $BASE

RUN set -euo pipefail && \
    zypper -n install --no-recommends grafana && \
    zypper -n clean && \
    rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.grafana
LABEL org.opencontainers.image.title="${PRODUCT} Grafana"
LABEL org.opencontainers.image.name=monitoring-grafana-image
LABEL org.opencontainers.image.description="Grafana container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/monitoring-grafana:%%grafana_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENV GF_PATHS_DATA="/var/lib/grafana"
ENV GF_PATHS_HOME="/usr/share/grafana"
ENV GF_PATHS_LOGS="/var/log/grafana"
ENV GF_PATHS_PLUGINS="/var/lib/grafana/plugins"
ENV GF_PATHS_PROVISIONING="/etc/grafana/provisioning"

ENTRYPOINT ["/run.sh"]
EXPOSE 3000
COPY run.sh /run.sh
RUN set -euo pipefail; chmod +x /run.sh

VOLUME /var/lib/grafana
