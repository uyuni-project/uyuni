ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

RUN zypper ref && \
      zypper -n install \
      java-11-openjdk-devel \
      openssh \
      rsync \
      apache-ivy \
      ant \
      ant-junit5 \
      servletapi5 \
      cpio \
      spacecmd \
      wget && \
    zypper addrepo --no-gpgcheck obs://systemsmanagement:Uyuni:Utils systemsmanagement:uyuni:utils && \
    zypper -n install obs-to-maven yarn && \
    zypper clean -a
COPY minima.yaml /etc/minima.yaml
COPY minima /usr/bin/minima
RUN /usr/bin/minima sync -c /etc/minima.yaml && \
    mv /srv/www/htdocs/pub/TestRepoRpmUpdates/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Updates/rpm/* /srv/www/htdocs/pub/TestRepoRpmUpdates/ && \
   rm -rf /srv/www/htdocs/pub/TestRepoRpmUpdates/repositories/
RUN cd /srv/www/htdocs/pub && ln -s TestRepoRpmUpdates AnotherRepo
RUN mkdir /etc/pki/rpm-gpg && wget -c http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/repodata/repomd.xml.key -O  /etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key

