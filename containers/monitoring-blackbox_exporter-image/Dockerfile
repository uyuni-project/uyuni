# SPDX-License-Identifier: MIT

#     Copyright (c) 2025 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/monitoring-blackbox_exporter:latest uyuni/monitoring-blackbox_exporter:%%blackbox_exporter_patch_version%%

ARG BASE=registry.suse.com/bci/bci-base:15.6
FROM $BASE

RUN set -euo pipefail && \
    zypper -n install --no-recommends \
        prometheus-blackbox_exporter \
        curl && \
    zypper -n clean && \
    rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.blackbox_exporter
LABEL org.opencontainers.image.title="${PRODUCT} Blackbox Exporter"
LABEL org.opencontainers.image.name=monitoring-blackbox_exporter-image
LABEL org.opencontainers.image.description="Blackbox Exporter container image for ${PRODUCT}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/monitoring-blackbox_exporter:%%blackbox_exporter_patch_version%%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT ["/usr/bin/blackbox_exporter"]
CMD ["--config.file=/etc/prometheus/blackbox.yml"]
EXPOSE 9115
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9115/-/healthy"]

