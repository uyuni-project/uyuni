# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/tumbleweed:latest

ENV ZYPPER_CMD="zypper --non-interactive"

# Installing all packages
RUN $ZYPPER_CMD addrepo --refresh --no-gpgcheck \
    "https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Tumbleweed/" uyuni-utils && \
    $ZYPPER_CMD addrepo --refresh --no-gpgcheck \
    "https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/ContainerUtils/openSUSE_Tumbleweed/" uyuni-tools && \
    $ZYPPER_CMD refresh && \
    $ZYPPER_CMD install --no-recommends \
    # Core System & Network
    docker systemd systemd-sysvinit shadow \
    glibc-locale iproute2 hostname \
    iputils sudo curl wget openssh \
    # Development Tools & Editors
    vim neovim tar gzip awk git \
    make man rsync cpio expect \
    bash-completion intltool \
    # Java Stack
    java-17-openjdk-devel \
    java-25-openjdk-devel \
    ant ant-junit ant-junit5 \
    apache-ivy maven servletapi5 \
    obs-to-maven \
    # Web & Python Stack
    nodejs22 npm22 python3 \
    python3-pip python3-PyYAML \
    python3-setuptools \
    # Uyuni Tools
    mgradm mgrctl podman \
    && $ZYPPER_CMD clean -a

ADD --chown=root:root root /

# Setting Java 17 as system default
RUN /usr/sbin/alternatives --set javac /usr/lib64/jvm/java-17-openjdk/bin/javac && \
        /usr/sbin/alternatives --set java /usr/lib64/jvm/jre-17-openjdk/bin/java && \
        /usr/sbin/alternatives --set jre_openjdk /usr/lib64/jvm/jre-17-openjdk && \
        /usr/sbin/alternatives --set java_sdk_openjdk /usr/lib64/jvm/java-17-openjdk

RUN cd /usr/lib/systemd/system/sysinit.target.wants && \
    find . -mindepth 1 -not -name "systemd-tmpfiles-setup.service" -delete && \
    rm -f /usr/lib/systemd/system/multi-user.target.wants/* \
    /etc/systemd/system/*.wants/* \
    /usr/lib/systemd/system/local-fs.target.wants/* \
    /usr/lib/systemd/system/sockets.target.wants/*udev* \
    /usr/lib/systemd/system/sockets.target.wants/*initctl* \
    /usr/lib/systemd/system/basic.target.wants/* \
    /usr/lib/systemd/system/anaconda.target.wants/*

RUN useradd --create-home --shell /bin/bash uyuni && \
    echo "uyuni:uyuni" | chpasswd && \
    mkdir -p /etc/sudoers.d && \
    echo 'uyuni ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/uyuni && \
    chmod 440 /etc/sudoers.d/uyuni

USER root
RUN chmod +x /etc/update-motd.d/*
RUN echo 'if [ -x /etc/update-motd.d/00-header ]; then /etc/update-motd.d/00-header; fi' >> /etc/bash.bashrc

VOLUME [ "/sys/fs/cgroup" ]
CMD ["/usr/sbin/init"]
