FROM opensuse/leap:15.4
# list of packages to installed are build dependencies from
# spacewalk-web package
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper ref -f && \ 
    zypper -n install \
      salt-master && \
    zypper clean -a
CMD /usr/bin/salt-master

