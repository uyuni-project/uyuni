# SPDX-License-Identifier: MIT

#     Copyright (c) 2024 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: uyuni/prometheus:latest

FROM suse/sle15:15.6

MAINTAINER openSUSE (https://www.opensuse.org/)

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"
ARG PATCH_VERSION="2.37.6"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.prometheus
LABEL org.opencontainers.image.title="${PRODUCT} Prometheus"
LABEL org.opencontainers.image.description="Prometheus container image for ${PRODUCT}."
LABEL org.opencontainers.image.version="${PATCH_VERSION}"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.source="%SOURCEURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/prometheus:${PATCH_VERSION}.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
# endlabelprefix

RUN set -euo pipefail; zypper -n in --no-recommends golang-github-prometheus-prometheus curl; zypper -n clean; rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}
ENTRYPOINT ["/usr/bin/prometheus"]
EXPOSE 9090
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9090/-/healthy"]

VOLUME /var/lib/prometheus

