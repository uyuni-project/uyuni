# SPDX-License-Identifier: MIT

#     Copyright (c) 2024 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/grafana:%%grafana_patch_version%%
#!BuildTag: uyuni/grafana:latest

ARG BASE=bci/bci-base:15.6
FROM $BASE

MAINTAINER SUSE LLC (https://www.suse.com/)

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.grafana
LABEL org.opencontainers.image.title="${PRODUCT} Grafana"
LABEL org.opencontainers.image.name="monitoring-grafana-image"
LABEL org.opencontainers.image.description="Grafana container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.1.3
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/grafana:%%grafana_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix

RUN set -euo pipefail; zypper -n in --no-recommends grafana; zypper -n clean; rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}
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
