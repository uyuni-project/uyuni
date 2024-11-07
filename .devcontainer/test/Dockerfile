FROM registry.opensuse.org/opensuse/leap:15.5
RUN zypper ref -f && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Leap_15.5/ tools && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby/15.5/ ruby && \
    zypper -n ar --no-gpgcheck http://download.opensuse.org/repositories/devel:/languages:/ruby:/extensions/15.5/ ruby_extensions && \
    zypper -n install nmap tar gzip iputils \
      gcc \
      make \
      wget \
      git \
      curl \
      libssh-devel \
      python-devel \
      ruby3.3 \
      ruby3.3-devel \
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
      twopence \
      python-twopence \
      twopence-devel \
      twopence-shell-client \
      twopence-test-server \
      rubygem-twopence \
      chromium \
      chromedriver \
      npm \
      openssh-server \
      openssh-clients \
      hostname \
      iproute2 \
      && \
    zypper clean -a
COPY etc_pam.d_sshd /etc/pam.d/sshd
RUN mkdir /root/.ssh
COPY id_rsa_test_env /root/.ssh/id_rsa
CMD ssh-keygen -A && /usr/sbin/sshd -De
RUN wget http://raw.githubusercontent.com/uyuni-project/uyuni/master/testsuite/Gemfile
RUN bundle.ruby3.3 install 
RUN rm Gemfile
RUN mkdir -p /root/.pki/nssdb
RUN certutil -d /root/.pki/nssdb -N
COPY bashrc /root/.bashrc
RUN source /root/.bashrc
