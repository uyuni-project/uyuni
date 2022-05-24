#!BuildTag: uyuni/proxy-squid:latest uyuni/proxy-squid:%PKG_VERSION% uyuni/proxy-squid:%PKG_VERSION%.%RELEASE%

ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE AS fat

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses squid

# Additional material
COPY squid.conf /etc/squid/squid.conf

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

# Prepare for slimming down
COPY prepare_target.sh .
RUN sh prepare_target.sh

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE AS slim

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML
COPY --from=fat /target /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-squid
LABEL org.opencontainers.image.title="${PRODUCT} proxy squid container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component cache http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="%PKG_VERSION%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-squid:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

# Squid
EXPOSE 8080/tcp

# Ensure every run / exec uses the squid user to avoid errors like
#    FATAL: Cannot open '/proc/self/fd/1' for writing.
# For this we need to give the squid user access to squid.conf and squid.pid
RUN chown squid:squid /etc/squid/squid.conf
RUN touch /run/squid.pid && chown squid:squid /run/squid.pid

# Ensure the cache is owned by squid user
RUN chown squid:squid /var/cache/squid

VOLUME [ "/etc/uyuni", "/var/cache/squid" ]

USER squid
CMD uyuni-configure.py && squid -z --foreground && squid -FC -d 1 --foreground
