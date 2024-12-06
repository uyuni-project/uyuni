FROM registry.opensuse.org/opensuse/leap:15.5
RUN zypper addrepo --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Leap_15.5 systemsmanagement:uyuni:utils
RUN zypper ref && \
      zypper -n install \
      java-17-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit \
      servletapi5 \
      cpio \
      spacecmd \
      expect \
      git \
      curl \
      wget && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
