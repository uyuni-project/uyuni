FROM docker.io/opensuse/leap:16.0@sha256:f239b4819f4dd322d99509f1b5b14f2107bf23857f9ccd3c14333f0928a2bcc6
# Ruby 3 devel repos
RUN zypper -n ar -f --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby/16.0/ ruby_devel

RUN zypper ref -f && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/16.0/ tools && \
    zypper -n install tar gzip iputils \
      gcc \
      make \
      wget \
      ruby3.4 \
      ruby3.4-devel \
      ruby3.4-devel-extra \
      python313-devel \
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
      postgresql16-devel \
      chromium \
      libgbm-devel \
      nodejs-default \
      npm-default \
      openssh-server \
      openssh-clients \
      hostname \
      iproute2 \
      vim \
      libssh4 \
      libssh-devel && \
    zypper -n clean -a
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De

### Ruby 3 changes ###
RUN export GEM_PATH="/usr/lib64/ruby/gems/3.4.0"
RUN echo 'GEM_PATH="/usr/lib64/ruby/gems/3.4.0"' >> ./root/.bashrc
RUN ln -sf /usr/bin/ruby.ruby3.4 /usr/bin/ruby
RUN ln -sf /usr/bin/gem.ruby3.4 /usr/bin/gem
RUN ln -sf /usr/bin/irb.ruby3.4 /usr/bin/irb
RUN update-alternatives --set rake /usr/bin/rake.ruby.ruby3.4
RUN update-alternatives --set bundle /usr/bin/bundle.ruby.ruby3.4
RUN update-alternatives --set rdoc /usr/bin/rdoc.ruby.ruby3.4
RUN update-alternatives --set ri /usr/bin/ri.ruby.ruby3.4

# debug
RUN ruby -v
RUN gem env

# Playwright CLI + Chromium for the Capybara Playwright driver.
# NOTE: no --with-deps — Playwright only supports apt/dnf for OS deps, not zypper.
# The distro `chromium` package (installed above) provides the required shared libraries.
# Keep PLAYWRIGHT_VERSION aligned with the `playwright-ruby-client` gem pin in testsuite/Gemfile.
ARG PLAYWRIGHT_VERSION=1.60.0
RUN npm install -g playwright@${PLAYWRIGHT_VERSION} && \
    playwright install chromium
ENV PLAYWRIGHT_CLI_EXECUTABLE_PATH=/usr/local/bin/playwright

ARG GEMFILE_BRANCH=master
RUN curl https://raw.githubusercontent.com/uyuni-project/uyuni/${GEMFILE_BRANCH}/testsuite/Gemfile -o Gemfile && bundle.ruby3.4 install --verbose && rm Gemfile
RUN gem list
