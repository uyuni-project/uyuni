# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/hub-xmlrpc-api:latest uyuni/hub-xmlrpc-api:4.4.0 uyuni/hub-xmlrpc-api:4.4.0.%RELEASE%

ARG INIT_BASE=registry.opensuse.org/bci/bci-micro
FROM $INIT_BASE

RUN zypper --gpg-auto-import-keys --non-interactive install hub-xmlrpc-api

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.hub-xmlrpc-api
LABEL org.opencontainers.image.title="${PRODUCT} Hub XML-RPC API container"
LABEL org.opencontainers.image.description="${PRODUCT} Hub XML-RPC API image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="4.4.0"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:4.4.0.%RELEASE%"
# endlabelprefix

CMD ["/usr/bin/hub-xmlrpc-api"]
