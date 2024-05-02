# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-attestation:latest

ARG BASE=registry.suse.com/bci/bci-base:15.5
FROM $BASE

# Architecture specific attestation modules are specified via project configuration
ARG ARCH_SPECIFIC_MODULES

# Main packages
RUN zypper ref && zypper --non-interactive up
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    uyuni-coco-attestation-core \
    uyuni-coco-attestation-module-secureboot \
    ${ARCH_SPECIFIC_MODULES} \
    javassist apache-commons-ognl

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-attestation
LABEL org.opencontainers.image.name=server-attestation-image
LABEL org.opencontainers.image.title="${PRODUCT} Confidential Computing attestation container"
LABEL org.opencontainers.image.description="${PRODUCT} Confidential Computing attestation container"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.0.2
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-attestation:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix

HEALTHCHECK --interval=5m --timeout=5s --retries=1 CMD ["pgrep", "-f", "/usr/sbin/coco-attestation"]

CMD ["/usr/sbin/coco-attestation"]
