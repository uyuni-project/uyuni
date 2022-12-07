FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Leap_15.4/ tools && \
    zypper -n install nmap tar gzip iputils \
      gcc \
      make \
      wget \
      ruby \
      ruby-devel \
      autoconf \
      ca-certificates-mozilla \
      automake \
      libtool \
      apache2-worker \
      cantarell-fonts \
      git-core \
      aaa_base-extras \
      zlib-devel \
      libxslt-devel \
      mozilla-nss-tools \
      postgresql-devel \
      ruby2.5-rubygem-bundler \
      twopence \
      python-twopence \
      twopence-devel \
      twopence-shell-client \
      twopence-test-server \
      rubygem-twopence \
      chromium \
      chromedriver \
      npm8 \
      && \
    zypper clean -a && \
    bundle.ruby2.5 install --gemfile testsuite/Gemfile && \
    npm install cucumber-htmlreporter@5.5.0 --save-dev

