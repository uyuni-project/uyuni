# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/mqtt-broker:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# Pre-create the mosquitto group and user with a static ID of 1883.
# This prevents zypper from allocating a dynamic UID/GID during package installation.
RUN groupadd -g 1883 mosquitto && \
    useradd -u 1883 -g 1883 -r -M -d /var/lib/mosquitto -s /sbin/nologin -c "Mosquitto Broker" mosquitto

# Install Mosquitto and clean up zypper cache/temporary files
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses mosquitto && \
    zypper --non-interactive clean --all && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Copy custom configurations
COPY mosquitto.conf /mosquitto/config/mosquitto.conf
COPY acl.conf /mosquitto/config/acl.conf

# Ensure directory structures are owned by the static 1883 user
RUN mkdir -p /mosquitto/data /mosquitto/log && \
    chown -R 1883:1883 /mosquitto

USER 1883
EXPOSE 1883

CMD ["/usr/sbin/mosquitto", "-c", "/mosquitto/config/mosquitto.conf"]
