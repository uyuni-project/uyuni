ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

# Set build argument for target architecture, defaulting to amd64 if not explicitly set.
ARG TARGETARCH=amd64

RUN echo "Check GH Runner IP: " && curl https://api.ipify.org

# Fix 1: Dynamically add the SLE-BCI repository based on TARGETARCH.
# SLE-BCI uses 'x86_64' for amd64 and 'aarch64' for arm64.
RUN ARCH_PATH="" && \
    if [ "${TARGETARCH}" = "amd64" ]; then \
        ARCH_PATH="x86_64"; \
    elif [ "${TARGETARCH}" = "arm64" ]; then \
        ARCH_PATH="aarch64"; \
    else \
        echo "Unsupported TARGETARCH: ${TARGETARCH}. Exiting." && exit 1; \
    fi && \
    zypper -n rr -a && \
    zypper addrepo https://installer-updates.suse.com/SUSE/Products/SLE-BCI/15-SP6/${ARCH_PATH}/product/ SLE_BCI && \
    zypper addrepo http://download.opensuse.org/distribution/leap/15.6/repo/oss/ openSUSE_Leap_15.6_OSS && \
    zypper addrepo http://download.opensuse.org/update/leap/15.6/oss/ openSUSE_Leap_15.6_Updates && \
    zypper -n --gpg-auto-import-keys ref && \
    zypper -n install --allow-downgrade --allow-vendor-change java-17-openjdk-devel && \
    zypper -n install \
      git \
      hostname \
      apache-ivy \
      ant \
      ant-junit && \
    zypper addrepo --no-gpgcheck --refresh https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/SLE_15_SP6/ systemsmanagement:uyuni:utils && \
    zypper addrepo --no-gpgcheck --refresh https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/SLE15-Uyuni-Client-Tools/SLE_15/systemsmanagement:Uyuni:Stable:SLE15-Uyuni-Client-Tools.repo && \
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

# Fix 3: Dynamically set the architecture for the Tumbleweed RPM mirroring, including the 'ports/aarch64' path for both URL and local directory.
RUN if [ "${TARGETARCH}" = "arm64" ]; then \
        ARCH_FOR_RPM="aarch64"; \
        TUMBLEWEED_PATH="ports/aarch64/tumbleweed"; \
        RPM_PATH_SUFFIX="aarch64"; \
        RPM_DIR_PREFIX="/mirror/ports/aarch64/tumbleweed/repo/oss"; \
    elif [ "${TARGETARCH}" = "amd64" ]; then \
        ARCH_FOR_RPM="x86_64"; \
        TUMBLEWEED_PATH="tumbleweed"; \
        RPM_PATH_SUFFIX="x86_64"; \
        RPM_DIR_PREFIX="/mirror/tumbleweed/repo/oss"; \
    else \
        echo "Unsupported TARGETARCH for RPM: ${TARGETARCH}. Exiting." && exit 1; \
    fi && \
    export SUSE_REPO="https://download.opensuse.org/${TUMBLEWEED_PATH}/repo/oss/" && \
    export RPM_DIR="${RPM_DIR_PREFIX}/${RPM_PATH_SUFFIX}" && \
    mkdir -p "${RPM_DIR}" && \
    cd "${RPM_DIR}" && \
    for PACKAGE in openSUSE-release-20 nodejs22 npm22 dmidecode libunwind golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter golang-github-QubitProducts-exporter_exporter; do \
        echo "Searching for latest version of ${PACKAGE}..." && \
        FULL_URL=$( \
            curl -sL "${SUSE_REPO}${RPM_PATH_SUFFIX}/" | \
            grep -oP "${PACKAGE}[^\"']+\.rpm" | \
            head -n 1 | \
            awk -v repo="${SUSE_REPO}${RPM_PATH_SUFFIX}/" '{print repo $1}' \
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
