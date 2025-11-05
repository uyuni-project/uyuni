ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

RUN zypper -n rr -a && \
    zypper addrepo https://installer-updates.suse.com/SUSE/Products/SLE-BCI/15-SP6/x86_64/product/ SLE_BCI && \
    zypper -n --gpg-auto-import-keys ref && \
    zypper -n install \
      java-17-openjdk-devel \
      rsync \
      apache-ivy \
      ant \
      ant-junit \
      servletapi5 \
      cpio \
      spacecmd && \
    zypper addrepo --no-gpgcheck --refresh https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/SLE_15_SP6/ systemsmanagement:uyuni:utils && \
    zypper addrepo --no-gpgcheck --refresh https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/SLE15-Uyuni-Client-Tools/SLE_15/systemsmanagement:Uyuni:Stable:SLE15-Uyuni-Client-Tools.repo && \
    zypper -n install obs-to-maven nodejs22 npm22 prometheus && \
    zypper clean -a

COPY minima.yaml /etc/minima.yaml

RUN mkdir /tmp/minima && \
    curl -L -o /tmp/minima/minima-linux-amd64.tar.gz https://github.com/moio/minima/releases/download/v0.10/minima-linux-amd64.tar.gz && \
    tar -zxvf /tmp/minima/minima-linux-amd64.tar.gz -C /tmp/minima && \
    cp /tmp/minima/minima /usr/bin/minima && \
    rm -rf /tmp/minima

RUN /usr/bin/minima sync -c /etc/minima.yaml && \
    mkdir /srv/www/htdocs/pub/TestRepoRpmUpdates /srv/www/htdocs/pub/TestRepoAppStream /srv/www/htdocs/pub/TestRepoDebUpdates&& \
    mv /srv/www/htdocs/pub/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Updates/rpm/* /srv/www/htdocs/pub/TestRepoRpmUpdates/ && \
    mv /srv/www/htdocs/pub/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Appstream/rhlike/* /srv/www/htdocs/pub/TestRepoAppStream/ && \
    mv /srv/www/htdocs/pub/repositories/systemsmanagement\:/Uyuni\:/Test-Packages\:/Updates/deb/* /srv/www/htdocs/pub/TestRepoDebUpdates/ && \
    rm -rf /srv/www/htdocs/pub/repositories/

RUN ln -s /srv/www/htdocs/pub/TestRepoRpmUpdates /srv/www/htdocs/pub/AnotherRepo

RUN mkdir -p /etc/pki/rpm-gpg && \
    curl -fsSL -o /etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/repodata/repomd.xml.key

COPY mirror /mirror

RUN export SUSE_REPO="https://download.opensuse.org/tumbleweed/repo/oss/" && \
    export RPM_DIR="/mirror/tumbleweed/repo/oss/x86_64" && \
    mkdir -p "${RPM_DIR}" && \
    cd "${RPM_DIR}" && \
    for PACKAGE in openSUSE-release-20 dmidecode libunwind golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter golang-github-QubitProducts-exporter_exporter; do \
        echo "Searching for latest version of ${PACKAGE}..." && \
        FULL_URL=$( \
            curl -sL "${SUSE_REPO}x86_64/" | \
            grep -oP "${PACKAGE}[^\"']+\.rpm" | \
            head -n 1 | \
            awk -v repo="${SUSE_REPO}x86_64/" '{print repo $1}' \
        ) && \
        if [ -n "$FULL_URL" ]; then \
            echo "Downloading ${FULL_URL}..." && \
            curl -L -O "${FULL_URL}"; \
        else \
            echo "Warning: Could not find RPM for ${PACKAGE}. Skipping."; \
        fi \
    done && \
    export METADATA_DIR="/mirror/tumbleweed/repo/oss/repodata" && \
    mkdir -p "${METADATA_DIR}" && \
    cd "${METADATA_DIR}" && \
    echo "Downloading repomd.xml..." && \
    curl -L -O "${SUSE_REPO}repodata/repomd.xml" && \
    for METADATA_TYPE in primary filelists other; do \
        echo "Searching for latest ${METADATA_TYPE}.xml.zst..." && \
        FULL_URL=$( \
            curl -sL "${SUSE_REPO}repodata/" | \
            grep -oP '[0-9a-f]{40}-'"${METADATA_TYPE}"'\.xml\.zst' | \
            head -n 1 | \
            awk -v repo="${SUSE_REPO}repodata/" '{print repo $1}' \
        ) && \
        if [ -n "$FULL_URL" ]; then \
            echo "Downloading ${FULL_URL}..." && \
            curl -L -O "${FULL_URL}"; \
        else \
            echo "Warning: Could not find ${METADATA_TYPE} metadata. Skipping."; \
        fi \
    done