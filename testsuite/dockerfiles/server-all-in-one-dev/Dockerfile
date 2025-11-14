ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

ARG TARGETPLATFORM
ENV TARGETARCH=${TARGETPLATFORM#*/}
RUN echo "TARGETPLATFORM=${TARGETPLATFORM}; TARGETARCH=${TARGETARCH}"

RUN if [ "${TARGETARCH}" = "amd64" ]; then \
        export ARCH_PATH="x86_64"; \
        export TUMBLEWEED_PATH="tumbleweed"; \
        export NODE_REPO_PATH="15.6"; \
    elif [ "${TARGETARCH}" = "arm64" ]; then \
        export ARCH_PATH="aarch64"; \
        export TUMBLEWEED_PATH="ports/aarch64/tumbleweed"; \
        export NODE_REPO_PATH="openSUSE_Factory_ARM"; \
    else \
        echo "Unsupported TARGETARCH: ${TARGETARCH}. Exiting." && exit 1; \
    fi && \
    export TUMBLEWEED_REPO="http://download.opensuse.org/${TUMBLEWEED_PATH}/repo/oss/" && \
    zypper -n rr -a && \
    zypper addrepo --no-gpgcheck --refresh https://installer-updates.suse.com/SUSE/Products/SLE-BCI/15-SP6/${ARCH_PATH}/product/ SLE_BCI && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/distribution/leap/15.6/repo/oss/ openSUSE_Leap_15.6_OSS && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/update/leap/15.6/oss/ openSUSE_Leap_15.6_Updates && \
    zypper -n install --allow-downgrade --allow-vendor-change \
      git \
      hostname \
      apache-ivy \
      ant \
      ant-junit && \
    zypper addrepo --no-gpgcheck --refresh ${TUMBLEWEED_REPO} tumbleweed_repo && \
    zypper -n install --allow-downgrade --allow-vendor-change java-17-openjdk-devel && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/devel:/languages:/nodejs/${NODE_REPO_PATH}/ nodejs && \
    zypper -n install --allow-downgrade --allow-vendor-change nodejs22 npm && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/SLE_15_SP6/ uyuni_utils && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/SLE15-Uyuni-Client-Tools/SLE_15/ SLE15-Uyuni-Client-Tools && \
    zypper -n install obs-to-maven prometheus && \
    zypper clean -a

COPY minima.yaml /etc/minima.yaml

RUN mkdir /tmp/minima && \
    curl -L -o /tmp/minima/minima-linux-${TARGETARCH}.tar.gz https://github.com/uyuni-project/minima/releases/download/v0.25/minima_0.25_linux_${TARGETARCH}.tar.gz && \
    tar -zxvf /tmp/minima/minima-linux-${TARGETARCH}.tar.gz -C /tmp/minima && \
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

RUN if [ "${TARGETARCH}" = "amd64" ]; then \
        export TUMBLEWEED_PATH="tumbleweed"; \
        export RPM_PATH_SUFFIX="x86_64"; \
        export RPM_DIR_PREFIX="/mirror/tumbleweed/repo/oss"; \
    elif [ "${TARGETARCH}" = "arm64" ]; then \
        export TUMBLEWEED_PATH="ports/aarch64/tumbleweed"; \
        export RPM_PATH_SUFFIX="aarch64"; \
        export RPM_DIR_PREFIX="/mirror/ports/aarch64/tumbleweed/repo/oss"; \
    else \
        echo "Unsupported TARGETARCH: ${TARGETARCH}. Exiting." && exit 1; \
    fi && \
    export TUMBLEWEED_REPO="http://download.opensuse.org/${TUMBLEWEED_PATH}/repo/oss/" && \
    \
    export RPM_DIR="${RPM_DIR_PREFIX}/${RPM_PATH_SUFFIX}" && \
    mkdir -p "${RPM_DIR}" && \
    cd "${RPM_DIR}" && \
    for PACKAGE in openSUSE-release-20 dmidecode libunwind golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter golang-github-QubitProducts-exporter_exporter; do \
        echo "Searching for latest version of ${PACKAGE}..." && \
        FULL_URL=$( \
            curl -sL "${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/" | \
            grep -oP "${PACKAGE}[^\"']+\.rpm" | \
            head -n 1 | \
            awk -v repo="${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/" '{print repo $1}' \
        ) && \
        if [ -n "$FULL_URL" ]; then \
            echo "Downloading ${FULL_URL}..." && \
            curl -L -O "${FULL_URL}"; \
        else \
            echo "Warning: Could not find RPM for ${PACKAGE}. Skipping."; \
        fi \
    done && \
    export METADATA_DIR="${RPM_DIR_PREFIX}/repodata" && \
    mkdir -p "${METADATA_DIR}" && \
    cd "${METADATA_DIR}" && \
    echo "Downloading repomd.xml..." && \
    curl -L -O "${TUMBLEWEED_REPO}repodata/repomd.xml" && \
    for METADATA_TYPE in primary filelists other; do \
        echo "Searching for latest ${METADATA_TYPE}.xml.zst..." && \
        FULL_URL=$( \
            curl -sL "${TUMBLEWEED_REPO}repodata/" | \
            grep -oP '[0-9a-f]{40}-'"${METADATA_TYPE}"'\.xml\.zst' | \
            head -n 1 | \
            awk -v repo="${TUMBLEWEED_REPO}repodata/" '{print repo $1}' \
        ) && \
        if [ -n "$FULL_URL" ]; then \
            echo "Downloading ${FULL_URL}..." && \
            curl -L -O "${FULL_URL}"; \
        else \
            echo "Warning: Could not find ${METADATA_TYPE} metadata. Skipping."; \
        fi \
    done
