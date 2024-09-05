ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

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
      wget && \
    zypper addrepo --no-gpgcheck obs://systemsmanagement:Uyuni:Utils systemsmanagement:uyuni:utils && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
COPY minima.yaml /etc/minima.yaml
RUN mkdir /tmp/minima && \
    cd /tmp/minima && \
    wget -c https://github.com/moio/minima/releases/download/v0.10/minima-linux-amd64.tar.gz && \
    tar zxvf minima-linux-amd64.tar.gz && \
    cp minima /usr/bin/minima
RUN /usr/bin/minima sync -c /etc/minima.yaml && \
    mkdir /srv/www/htdocs/pub/TestRepoRpmUpdates /srv/www/htdocs/pub/TestRepoAppstream && \
    mv /srv/www/htdocs/pub/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Updates/rpm/* /srv/www/htdocs/pub/TestRepoRpmUpdates/ && \
    mv /srv/www/htdocs/pub/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Appstream/rhlike/* /srv/www/htdocs/pub/TestRepoAppStream/ && \
    rm -rf /srv/www/htdocs/pub/repositories/
RUN cd /srv/www/htdocs/pub && ln -s TestRepoRpmUpdates AnotherRepo
RUN mkdir /etc/pki/rpm-gpg && wget -c http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/repodata/repomd.xml.key -O  /etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key

