# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/node-red:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
    nodejs22 npm22 && \
    zypper --non-interactive clean --all && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# User setup
RUN groupadd -g 1880 nodered && \
    useradd -u 1880 -g 1880 -r -m -d /home/nodered -s /sbin/nologin -c "Node-RED" nodered

# Node-RED
RUN npm install -g --unsafe-perm node-red@4.0.9

# Uyuni nodes - pack as tarball to resolve dependencies properly
RUN mkdir -p /opt/node-red-contrib-uyuni
COPY --chown=nodered:nodered node-red-contrib-uyuni/ /opt/node-red-contrib-uyuni/


RUN cd /opt/node-red-contrib-uyuni && \
    npm pack --pack-destination /tmp && \
    npm install -g --unsafe-perm --omit=dev --no-audit --no-fund /tmp/node-red-contrib-uyuni-*.tgz && \
    rm -f /tmp/node-red-contrib-uyuni-*.tgz

# Configuration (build context is repo root)
COPY --chmod=555 containers/node-red-image/uyuni-configure.sh /usr/bin/uyuni-configure.sh
COPY --chmod=644 containers/node-red-image/settings.js.template /opt/settings.js.template

# Data volume
RUN mkdir -p /data && chown -R 1880:1880 /data

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"


# labelprefix=org.opensuse.uyuni.node-red
LABEL org.opencontainers.image.title="${PRODUCT} Node-RED automation container"
LABEL org.opencontainers.image.description="Node-RED with pre-installed ${PRODUCT} automation nodes for visual event-driven workflows"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=node-red-image
LABEL org.opencontainers.image.version=5.2.8
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/node-red:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

USER 1880
EXPOSE 1880

VOLUME "/data"

CMD ["uyuni-configure.sh"]
