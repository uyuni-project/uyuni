#!BuildTag: health-check-supportconfig-exporter

ARG BASE=registry.suse.com/bci/bci-base:15.6

FROM $BASE

RUN zypper -n ref
RUN zypper -n install python311-PyYAML python311-pip python311-prometheus-client python311-Jinja2 python311-rich

# Build Service required labels
# labelprefix=org.opensuse.uyuni.health-check-supportconfig-exporter
LABEL org.opencontainers.image.name=health-check-supportconfig-exporter-image
LABEL org.opencontainers.image.title="${PRODUCT} health check supportconfig-exporter container"
LABEL org.opencontainers.image.description="${PRODUCT} health check supportconfig-exporter image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=0.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/health-check-supportconfig-exporter:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

WORKDIR /opt/
ENTRYPOINT ["python3.11", "/opt/supportconfig_exporter.py"]

