# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/health-check-supportconfig-exporter:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7

FROM $BASE

RUN zypper --non-interactive ref && zypper --non-interactive up && \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
        python311-PyYAML

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.health-check-supportconfig-exporter
LABEL org.opencontainers.image.name=health-check-supportconfig-exporter-image
LABEL org.opencontainers.image.title="${PRODUCT} health check supportconfig-exporter container"
LABEL org.opencontainers.image.description="${PRODUCT} health check supportconfig-exporter image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/health-check-supportconfig-exporter:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

WORKDIR /opt/
ENTRYPOINT ["python3.11", "/opt/supportconfig_exporter.py"]
