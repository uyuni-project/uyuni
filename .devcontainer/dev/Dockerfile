# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/tumbleweed:latest
RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Tumbleweed systemsmanagement:uyuni:utils
RUN zypper ref && \
    zypper -n install \
    ant \
    ant-junit \
    ant-junit5 \
    apache-ivy \
    awk \
    bash-completion \
    cpio \
    curl \
    expect \
    git \
    java-17-openjdk-devel \
    make \
    man \
    maven \
    neovim \
    nodejs22 \
    npm22 \
    obs-to-maven \
    openssh \
    python3-PyYAML \
    python3-pip \
    python3-setuptools \
    rsync \
    servletapi5 \
    sudo \
    tar \
    wget \
    yarn && \
    zypper clean -a
