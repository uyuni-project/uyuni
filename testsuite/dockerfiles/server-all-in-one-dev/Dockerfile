ARG BASE
ARG VERSION
FROM ${BASE}:${VERSION}

ARG TARGETPLATFORM
ENV TARGETARCH=${TARGETPLATFORM#*/}
RUN echo "TARGETPLATFORM=${TARGETPLATFORM}; TARGETARCH=${TARGETARCH}"

RUN if [ "${TARGETARCH}" = "amd64" ]; then \
        export ARCH_PATH="x86_64"; \
        export TUMBLEWEED_PATH="tumbleweed"; \
        export NODE_REPO_PATH="16.0"; \
    elif [ "${TARGETARCH}" = "arm64" ]; then \
        export ARCH_PATH="aarch64"; \
        export TUMBLEWEED_PATH="ports/aarch64/tumbleweed"; \
        export NODE_REPO_PATH="openSUSE_Factory_ARM"; \
    else \
        echo "Unsupported TARGETARCH: ${TARGETARCH}. Exiting." && exit 1; \
    fi && \
    export TUMBLEWEED_REPO="http://download.opensuse.org/${TUMBLEWEED_PATH}/repo/oss/" && \
    zypper -n rr -a && \
    zypper addrepo --no-gpgcheck --refresh https://installer-updates.suse.com/SUSE/Products/SLE-BCI/16.0/${ARCH_PATH}/product/ SLE_BCI && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/distribution/leap/16.0/repo/oss/ openSUSE_Leap_16.0_OSS && \
    zypper -n install --allow-downgrade --allow-vendor-change \
      openssh-server \
      git \
      hostname \
      createrepo_c \
      apache-ivy \
      ant \
      ant-junit && \
    zypper addrepo --no-gpgcheck --refresh ${TUMBLEWEED_REPO} tumbleweed_repo && \
    zypper -n install --allow-downgrade --allow-vendor-change java-17-openjdk-devel && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/devel:/languages:/nodejs/${NODE_REPO_PATH}/ nodejs && \
    zypper -n install --allow-downgrade --allow-vendor-change nodejs22 npm && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Utils/openSUSE_Leap_16.0/ uyuni_utils && \
    zypper addrepo --no-gpgcheck --refresh http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/SLE16-Uyuni-Client-Tools/SLE_16/ SLE16-Uyuni-Client-Tools && \
    zypper -n install obs-to-maven prometheus && \
    zypper clean -a

COPY minima.yaml /etc/minima.yaml

RUN mkdir /tmp/minima && \
    curl -L -o /tmp/minima/minima-linux-${TARGETARCH}.tar.gz https://github.com/uyuni-project/minima/releases/download/v0.27/minima_0.27_linux_${TARGETARCH}.tar.gz && \
    case "${TARGETARCH}" in \
      amd64) echo "0e66d850742229b36cf22ff3a05ec040537bbd7933d75abccf910254e1a61cb4 /tmp/minima/minima-linux-${TARGETARCH}.tar.gz" | sha256sum -c - ;; \
      arm64) echo "b4c1ea1552d011c2ed1877acd515ea7be24783c22fdc94192048a5f14fcc574a  /tmp/minima/minima-linux-${TARGETARCH}.tar.gz" | sha256sum -c - ;; \
      *) echo "Unsupported TARGETARCH: ${TARGETARCH}" && exit 1 ;; \
    esac && \
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
    echo "Listing ${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/..." && \
    PACKAGE_LIST=$(curl -sSfL --retry 5 --retry-delay 5 --retry-all-errors "${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/") && \
    for PACKAGE in openSUSE-release-20 dmidecode libunwind golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter golang-github-QubitProducts-exporter_exporter; do \
        echo "Searching for latest version of ${PACKAGE}..." && \
        FULL_URL=$( \
            echo "${PACKAGE_LIST}" | \
            grep -oP "${PACKAGE}[^\"']+\.rpm" | \
            head -n 1 | \
            awk -v repo="${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/" '{print repo $1}' \
        ) && \
        if [ -n "$FULL_URL" ]; then \
            echo "Downloading ${FULL_URL}..." && \
            { curl -fL --retry 5 --retry-delay 5 --retry-all-errors -O "${FULL_URL}" || exit 1; }; \
        else \
            echo "Could not find the RPM of ${PACKAGE} in ${TUMBLEWEED_REPO}${RPM_PATH_SUFFIX}/." && exit 1; \
        fi \
    done && \
    echo "Generating the metadata of the mirrored packages..." && \
    createrepo_c "${RPM_DIR_PREFIX}"
