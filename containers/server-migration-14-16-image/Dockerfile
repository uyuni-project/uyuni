# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-migration-14-16:latest

ARG INIT_IMAGE=uyuni/init:latest
FROM $INIT_IMAGE

# Main packages
RUN zypper ref 
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    postgresql16-server \
    postgresql16-contrib \
    postgresql14-server \
    postgresql14-contrib

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-migration-14-16
LABEL org.opencontainers.image.name=server-migration-14-16-image
LABEL org.opencontainers.image.title="${PRODUCT} server migration container"
LABEL org.opencontainers.image.description="${PRODUCT} server migration image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server-migration-14-16:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

