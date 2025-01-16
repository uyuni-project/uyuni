# SPDX-FileCopyrightText: 2024 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/leap:15.6
RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Leap_15.6 systemsmanagement:uyuni:utils
RUN zypper ref && \
      zypper -n install \
      java-17-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit5 \
      ant-junit \
      servletapi5 \
      cpio \
      python3-PyYAML \
      python3-pip \
      spacecmd \
      expect \
      git \
      curl \
      make \
      sudo \
      neovim \
      wget && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
