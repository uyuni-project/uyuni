# SPDX-License-Identifier: MIT

#     Copyright (c) 2024 SUSE LLC

# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon.

#!BuildTag: opensuse/prometheus:latest

FROM opensuse/leap:15.6

MAINTAINER openSUSE (https://www.opensuse.org/)

ARG PATCH_VERSION="2.53.0"

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.application.prometheus
LABEL org.opencontainers.image.title="openSUSE Tumbleweed Prometheus"
LABEL org.opencontainers.image.description="Prometheus container based on the openSUSE Tumbleweed Base Container Image."
LABEL org.opencontainers.image.version="${PATCH_VERSION}"
LABEL org.opencontainers.image.url="https://www.opensuse.org"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="openSUSE Project"
LABEL org.opencontainers.image.source="%SOURCEURL%"
LABEL org.opensuse.reference="registry.opensuse.org/opensuse/prometheus:%%prometheus_patch_version%%-%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.lifecycle-url="https://en.opensuse.org/Lifetime"
LABEL org.opensuse.release-stage="released"
# endlabelprefix

RUN set -euo pipefail; zypper -n in --no-recommends golang-github-prometheus-prometheus curl; zypper -n clean; rm -rf /var/log/{lastlog,tallylog,zypper.log,zypp/history,YaST2}
ENTRYPOINT ["/usr/bin/prometheus"]
EXPOSE 9090
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
    CMD ["/usr/bin/curl", "-m", "2", "-sf", "http://localhost:9090/-/healthy"]

VOLUME /var/lib/prometheus

