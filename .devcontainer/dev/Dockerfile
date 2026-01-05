# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
FROM registry.opensuse.org/opensuse/tumbleweed:latest

# Installing all packages
RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Tumbleweed systemsmanagement:uyuni:utils && \
        zypper refresh && \
        zypper -n install awk bash-completion curl expect git iputils make man neovim openssh rsync sudo tar vim wget \
                          intltool cpio java-25-openjdk-devel java-17-openjdk-devel \
                          nodejs22 npm22 \
                          ant ant-junit ant-junit5 apache-ivy maven servletapi5 \
                          python3 python3-pip python3-PyYAML python3-setuptools \
                          obs-to-maven && \
         zypper clean -a

# Setting Java 17 as system default
RUN /usr/sbin/alternatives --set javac /usr/lib64/jvm/java-17-openjdk/bin/javac && \
        /usr/sbin/alternatives --set java /usr/lib64/jvm/jre-17-openjdk/bin/java && \
        /usr/sbin/alternatives --set jre_openjdk /usr/lib64/jvm/jre-17-openjdk && \
        /usr/sbin/alternatives --set java_sdk_openjdk /usr/lib64/jvm/java-17-openjdk

# Creating regular user
RUN useradd --create-home --shell /bin/bash uyuni
RUN echo "uyuni ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

USER uyuni

COPY initialize.sh /home/uyuni/initialize.sh
