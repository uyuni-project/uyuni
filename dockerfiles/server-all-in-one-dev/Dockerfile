FROM ghcr.io/jordimassaguerpla/uyuni/server-all-in-one:master

RUN zypper -n install \
      java-11-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit5 \
      servletapi5 \
      cpio && \
    zypper addrepo --no-gpgcheck obs://systemsmanagement:Uyuni:Utils systemsmanagement:uyuni:utils && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a

