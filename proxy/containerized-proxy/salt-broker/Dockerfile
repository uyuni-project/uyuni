FROM registry.suse.com/suse/sle15

# Salt
EXPOSE 4505
EXPOSE 4506

# provides python3-debian
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ openSUSE-Leap-15.3-Oss

RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ Proxy

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-salt
