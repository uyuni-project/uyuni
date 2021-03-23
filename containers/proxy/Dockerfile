# rebuild with: docker build -t proxy .

FROM registry.opensuse.org/opensuse/leap:15.2

# Apache
EXPOSE 80
EXPOSE 443
# Salt
EXPOSE 4505
EXPOSE 4506

RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ Proxy

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses patterns-uyuni_proxy salt-minion openssh

# only for development
RUN zypper --gpg-auto-import-keys --non-interactive install vim less

RUN  mkdir -p /root/salt
COPY salt /root/salt

CMD [ "/bin/bash" ]
