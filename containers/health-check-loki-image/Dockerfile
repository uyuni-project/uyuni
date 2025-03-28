#!BuildTag: uyuni/health-check-loki:latest

ARG BASE=registry.suse.com/bci/bci-base:15.6

FROM $BASE

RUN zypper -n ref
RUN zypper -n install loki

# Build Service required labels
# labelprefix=org.opensuse.uyuni.health-check-loki
LABEL org.opencontainers.image.name=health-check-loki-image
LABEL org.opencontainers.image.title="${PRODUCT} health check loki container"
LABEL org.opencontainers.image.description="${PRODUCT} health check loki image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/health-check-loki:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT [ "/usr/bin/loki" ]
CMD ["-config.file=/etc/loki/local-config.yaml"]
