# SPDX-License-Identifier: MIT

#     Copyright (c) 2024 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

ARG PATCH_VERSION="0.24.0"

#!BuildTag: suse/alertmanager:${PATCH_VERSION}
#!BuildTag: suse/alertmanager:latest

ARG BASE=bci/bci-base:15.6
FROM $BASE

MAINTAINER SUSE LLC (https://www.suse.com/)

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.alertmanager
LABEL org.opencontainers.image.title="${PRODUCT} Blackbox Exporter"
LABEL org.opencontainers.image.description="Blackbox Exporter container image for ${PRODUCT}."
LABEL org.opencontainers.image.version="${PATCH_VERSION}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/blackbox_exporter:${PATCH_VERSION}.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix

RUN set -euo pipefail; zypper -n in --no-recommends prometheus-blackbox_exporter=${PATCH_VERSION} curl; zypper -n clean; rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}
ENTRYPOINT ["/usr/bin/blackbox_exporter"]
CMD ["--config.file=/etc/prometheus/blackbox.yml"]
EXPOSE 9115
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9115/-/healthy"]

