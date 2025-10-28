# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-saline:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

RUN groupadd -r --gid 10554 salt && \
    useradd -r -s /usr/sbin/nologin -g salt -d /var/lib/salt --uid 10554 salt

# Main packages
RUN zypper ref -f && zypper --non-interactive up
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution --download-only saline python311-saline
RUN find /var/cache/zypp/packages/ -type f -name '*.rpm'
RUN rpm -ivh --nodeps $(find /var/cache/zypp/packages/ -type f \
    -name update-alternatives.rpm -o \
    -name python311-base.rpm -o -name 'libpython3_11*.rpm' -o -name 'libopenssl*.rpm' -o -name libexpat1.rpm -o \
    -name salt.rpm -o -name python311-salt.rpm -o -name python311-tornado.rpm -o \
    -name python311-contextvars.rpm -o -name python311-immutables.rpm -o -name python311-looseversion.rpm -o \
    -name python311-packaging.rpm -o -name python311-distro.rpm -o \
    -name python311-PyYAML.rpm -o -name 'libyaml*.rpm' -o -name python311-Jinja2.rpm -o -name python311-MarkupSafe.rpm -o \
    -name python311-msgpack.rpm -o -name python311-more-itertools.rpm -o \
    -name saline.rpm -o -name python311-saline.rpm -o -name python311-python-dateutil.rpm -o -name python311-six.rpm)
RUN zypper clean --all

RUN mkdir /etc/saline.defaults && cp -r /etc/salt/saline* /etc/saline.defaults/
RUN chown -R salt:salt /etc/saline.defaults

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-saline
LABEL org.opencontainers.image.title="${PRODUCT} server saline container"
LABEL org.opencontainers.image.description="Image contains the ${PRODUCT} server component to extend Salt capabilities"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=server-saline-image
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/salt-saline:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix

COPY cleanup.sh /cleanup.sh
RUN /bin/bash /cleanup.sh ; rm /cleanup.sh

COPY run_saline.sh /run_saline.sh
CMD ["/bin/bash", "/run_saline.sh"]
