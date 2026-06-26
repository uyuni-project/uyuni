FROM eclipse-mosquitto:2.0.18

# Copy custom configurations
COPY mosquitto.conf /mosquitto/config/mosquitto.conf
COPY acl.conf /mosquitto/config/acl.conf

# Ensure directory permissions (eclipse-mosquitto runs as uid/gid 1883)
USER root
RUN mkdir -p /mosquitto/data /mosquitto/log && \
    chown -R mosquitto:mosquitto /mosquitto/data /mosquitto/log /mosquitto/config

USER mosquitto
