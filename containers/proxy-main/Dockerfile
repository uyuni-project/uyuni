# rebuild with: docker build -t proxy .

FROM registry.opensuse.org/opensuse/leap:15.2

# Apache
EXPOSE 80
EXPOSE 443

RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ Proxy

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
 salt-minion openssh \
 python3-rhnlib spacewalk-proxy-broker \
 spacewalk-proxy-common spacewalk-proxy-management spacewalk-proxy-package-manager \
 spacewalk-proxy-redirect spacewalk-ssl-cert-check \
 Uyuni-Proxy-release

# we have dependencies on spacewalk-proxy packages
RUN rpm -e --nodeps squid

# only for development
RUN zypper --gpg-auto-import-keys --non-interactive install vim less iputils

RUN  mkdir -p /root/salt
COPY salt /root/salt

CMD salt-call --local --log-level=debug --file-root=/root/salt/ state.highstate && sh /root/salt/proxy/run.sh
