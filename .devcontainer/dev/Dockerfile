# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/tumbleweed:latest

RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Tumbleweed systemsmanagement:uyuni:utils && \
        zypper refresh && \
        zypper -n install awk bash-completion curl expect git iputils make man neovim openssh rsync sudo tar vim wget \
                          nodejs22 npm22 \
                          ant ant-junit ant-junit5 apache-ivy cpio java-17-openjdk-devel maven servletapi5 \
                          python3 python3-pip python3-PyYAML python3-setuptools \
                          obs-to-maven && \
         zypper clean -a

RUN useradd --create-home --shell /bin/bash uyuni
RUN echo "uyuni ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

USER uyuni

COPY initialize.sh /home/uyuni/initialize.sh
