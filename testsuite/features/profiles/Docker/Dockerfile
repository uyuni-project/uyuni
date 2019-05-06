FROM registry.mgr.suse.de/toaster-sles12sp3-products
MAINTAINER Admin User "noemail@example.com"

ARG repo
ARG cert

RUN echo "$cert" > /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT.pem
RUN update-ca-certificates
RUN echo "$repo" > /etc/zypp/repos.d/susemanager:dockerbuild.repo

ADD avahi/avahi-0.6.32-30.36.x86_64.rpm /root/avahi-0.6.32-30.36.x86_64.rpm
ADD avahi/libavahi-common3-0.6.32-30.36.x86_64.rpm /root/libavahi-common3-0.6.32-30.36.x86_64.rpm
ADD avahi/libavahi-core7-0.6.32-30.36.x86_64.rpm /root/libavahi-core7-0.6.32-30.36.x86_64.rpm
ADD avahi/libdaemon0-0.14-14.57.x86_64.rpm /root/libdaemon0-0.14-14.57.x86_64.rpm
ADD avahi/nss-mdns-0.10-64.1.x86_64.rpm /root/nss-mdns-0.10-64.1.x86_64.rpm

ADD nsswitch.conf /etc/nsswitch.conf
ADD avahi-daemon.conf /root/avahi-daemon.conf

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
