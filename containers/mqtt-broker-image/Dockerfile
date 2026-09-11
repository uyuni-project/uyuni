# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/mqtt-broker:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# User setup
RUN groupadd -g 1883 mosquitto && \
    useradd -u 1883 -g 1883 -r -M -d /var/lib/mosquitto -s /sbin/nologin -c "Mosquitto Broker" mosquitto

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses mosquitto && \
    zypper --non-interactive clean --all && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Configuration
COPY mosquitto.conf /mosquitto/config/mosquitto.conf
COPY --chmod=600 acl.conf /mosquitto/config/acl.conf
COPY --chmod=555 uyuni-configure.sh /usr/bin/uyuni-configure.sh

# Permissions
RUN mkdir -p /mosquitto/data && \
    chown -R 1883:1883 /mosquitto

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.mqtt-broker
LABEL org.opencontainers.image.title="${PRODUCT} MQTT broker container"
LABEL org.opencontainers.image.description="Image contains a Mosquitto broker distributing ${PRODUCT} events"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=mqtt-broker-image
LABEL org.opencontainers.image.version=5.2.8
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/mqtt-broker:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

USER 1883
EXPOSE 1883

VOLUME "/mosquitto/data"

CMD uyuni-configure.sh && exec /usr/sbin/mosquitto -c /mosquitto/config/mosquitto.conf
