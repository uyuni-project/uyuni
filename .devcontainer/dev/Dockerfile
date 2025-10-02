# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/tumbleweed
RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Tumbleweed systemsmanagement:uyuni:utils
RUN zypper ref && \
      zypper -n install \
      java-17-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit5 \
      ant-junit \
      awk \
      servletapi5 \
      cpio \
      python3-PyYAML \
      python3-pip \
      python3-setuptools \
      expect \
      git \
      curl \
      make \
      sudo \
      neovim \
      tar \
      wget && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
