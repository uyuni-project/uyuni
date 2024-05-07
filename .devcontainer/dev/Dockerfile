FROM registry.opensuse.org/opensuse/leap:15.5
RUN zypper ref && \
      zypper -n install \
      java-17-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit5 \
      servletapi5 \
      cpio \
      spacecmd \
      expect \
      git \
      curl \
      wget && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
