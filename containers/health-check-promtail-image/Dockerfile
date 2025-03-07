#!BuildTag: uyuni/health-check-promtail:latest

FROM registry.suse.com/bci/bci-base:15.6

RUN zypper -n install promtail

# Build Service required labels
# labelprefix=org.opensuse.uyuni.health-check-promtail
LABEL org.opencontainers.image.name=health-check-promtail-image
LABEL org.opencontainers.image.title="${PRODUCT} health check promtail container"
LABEL org.opencontainers.image.description="${PRODUCT} health check promtail image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=0.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/health-check-promtail:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

ENTRYPOINT ["/usr/bin/promtail"]
CMD ["-config.file=/etc/promtail/config.yml"]
