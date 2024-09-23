FROM opensuse/leap:15.5
# Ruby 3 devel repos
RUN zypper -n ar -f --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby/15.5/ ruby_devel
RUN zypper -n ar -f --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby:/extensions/15.5/ rubey_devel_extensions

RUN zypper ref -f && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Leap_15.5/ tools && \
    zypper -n install nmap tar gzip iputils \
      gcc \
      make \
      wget \
      ruby3.3 \
      ruby3.3-devel \
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
      npm \
      openssh-server \
      openssh-clients \
      hostname \
      iproute2 \
      && \
      zypper -n install --oldpackage libssh4=0.9.6 && \
      zypper -n install --oldpackage libssh-devel=0.9.6 && \
    zypper -n clean -a
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De

### Ruby 3 changes ###
RUN export GEM_PATH="/usr/lib64/ruby/gems/3.3.0"
RUN echo 'GEM_PATH="/usr/lib64/ruby/gems/3.3.0"' >> ./root/.bashrc
RUN ln -sf /usr/bin/ruby.ruby3.3 /usr/bin/ruby
RUN ln -sf /usr/bin/gem.ruby3.3 /usr/bin/gem
RUN ln -sf /usr/bin/irb.ruby3.3 /usr/bin/irb
RUN update-alternatives --set rake /usr/bin/rake.ruby.ruby3.3
RUN update-alternatives --set bundle /usr/bin/bundle.ruby.ruby3.3
RUN update-alternatives --set rdoc /usr/bin/rdoc.ruby.ruby3.3
RUN update-alternatives --set ri /usr/bin/ri.ruby.ruby3.3

# debug
RUN ruby -v
RUN gem env

# TODO: Adjust the URL/branch before merging!
RUN curl https://raw.githubusercontent.com/uyuni-project/uyuni/qe-develop/testsuite/Gemfile -o Gemfile && bundle.ruby3.3 install --verbose && rm Gemfile
RUN gem list
