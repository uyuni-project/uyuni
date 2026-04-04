FROM registry.mgr.suse.de/suse/sle15:15.7
MAINTAINER Admin User "noemail@example.com"

ARG repo
ARG cert

RUN echo "$cert" > /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT.pem
RUN update-ca-certificates
RUN echo "$repo" > /etc/zypp/repos.d/susemanager:dockerbuild.repo

ADD nsswitch.conf /etc/nsswitch.conf
ADD avahi-daemon.conf /root/avahi-daemon.conf
ADD sles15sp7.repo /etc/zypp/repos.d/sles15sp7.repo

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
