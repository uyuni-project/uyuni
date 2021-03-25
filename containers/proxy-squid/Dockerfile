# rebuild with: docker build -t proxy .

FROM registry.opensuse.org/opensuse/leap:15.2

# squid
EXPOSE 8080

RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ Proxy

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses squid salt

# only for development
RUN zypper --gpg-auto-import-keys --non-interactive install vim less iputils

RUN  mkdir -p /root/salt
COPY salt /root/salt

CMD salt-call --local --log-level=debug --file-root=/root/salt/ state.apply proxy.user,proxy.squid && /usr/sbin/squid -FC --foreground
