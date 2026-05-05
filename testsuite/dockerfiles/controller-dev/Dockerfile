FROM opensuse/leap:15.6
# Ruby devel repos
RUN zypper -n ar -f --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby/15.6/ ruby_devel
RUN zypper -n ar -f --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby:/extensions/15.6/ rubey_devel_extensions

RUN zypper ref -f && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Leap_15.6/ tools && \
    zypper -n install nmap tar gzip iputils \
      gcc \
      make \
      wget \
      ruby4.0 \
      ruby4.0-devel \
      python-devel \
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
      postgresql14-devel \
      chromium \
      chromedriver \
      libgbm-devel \
      npm \
      openssh-server \
      openssh-clients \
      hostname \
      iproute2 \
      vi \
      libssh4 \
      libssh-devel && \
    zypper -n clean -a
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De

### Ruby 3 changes ###
RUN export GEM_PATH="/usr/lib64/ruby/gems/4.0.0"
RUN echo 'GEM_PATH="/usr/lib64/ruby/gems/4.0.0"' >> ./root/.bashrc
RUN ln -sf /usr/bin/ruby.ruby4.0 /usr/bin/ruby
RUN ln -sf /usr/bin/gem.ruby4.0 /usr/bin/gem
RUN ln -sf /usr/bin/irb.ruby4.0 /usr/bin/irb
RUN update-alternatives --set rake /usr/bin/rake.ruby.ruby4.0
RUN update-alternatives --set bundle /usr/bin/bundle.ruby.ruby4.0
RUN update-alternatives --set rdoc /usr/bin/rdoc.ruby.ruby4.0
RUN update-alternatives --set ri /usr/bin/ri.ruby.ruby4.0

# debug
RUN ruby -v
RUN gem env

# TODO: Adjust the URL/branch before merging!
RUN curl https://raw.githubusercontent.com/uyuni-project/uyuni/master/testsuite/Gemfile -o Gemfile && bundle.ruby4.0 install --verbose && rm Gemfile
RUN gem list
