# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server-saline:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"
ARG USEPYTHON="python311"

RUN groupadd -r --gid 10554 salt && \
    useradd -r -s /usr/sbin/nologin -g salt -d /var/lib/salt --uid 10554 salt

COPY run_saline.sh /run_saline.sh

# We need to install find before we can run the installation of saline packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution findutils

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution --download-only saline ${USEPYTHON}-saline && \
    rpm -ivh --nodeps $(find /var/cache/zypp/packages/ -type f \
      -name update-alternatives.rpm -o \
      -name ${USEPYTHON}-base.rpm -o -name lib${USEPYTHON/python3/python3_/}\*.rpm -o -name 'libopenssl*.rpm' -o -name libexpat1.rpm -o \
      -name salt.rpm -o -name ${USEPYTHON}-salt.rpm -o -name ${USEPYTHON}-tornado6.rpm -o \
      -name ${USEPYTHON}-contextvars.rpm -o -name ${USEPYTHON}-immutables.rpm -o -name ${USEPYTHON}-looseversion.rpm -o \
      -name ${USEPYTHON}-packaging.rpm -o -name ${USEPYTHON}-distro.rpm -o \
      -name ${USEPYTHON}-PyYAML.rpm -o -name 'libyaml*.rpm' -o -name ${USEPYTHON}-Jinja2.rpm -o -name ${USEPYTHON}-MarkupSafe.rpm -o \
      -name ${USEPYTHON}-msgpack.rpm -o -name ${USEPYTHON}-more-itertools.rpm -o \
      -name saline.rpm -o -name ${USEPYTHON}-saline.rpm -o -name ${USEPYTHON}-python-dateutil.rpm -o -name ${USEPYTHON}-six.rpm) && \
    mkdir /etc/saline.defaults && cp -r /etc/salt/saline* /etc/saline.defaults/ && \
    chown -R salt:salt /etc/saline.defaults && \
    zypper --non-interactive clean --all && \
    rpm -e zypper libzypp container-suseconnect && \
    find /etc -maxdepth 1 -type f -name '*-' -delete && \
    rm -rf /etc/salt /run/zypp.pid /srv/spm /usr/lib/rpm/macros.d/macros.python3 \
           /usr/share/doc /usr/share/licenses /usr/share/man && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server-saline
LABEL org.opencontainers.image.title="${PRODUCT} server saline container"
LABEL org.opencontainers.image.description="Image contains the ${PRODUCT} server component to extend Salt capabilities"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=server-saline-image
LABEL org.opencontainers.image.version=5.2.9
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/salt-saline:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix

CMD ["/bin/bash", "/run_saline.sh"]
