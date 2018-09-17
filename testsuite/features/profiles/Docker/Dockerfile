FROM registry.mgr.suse.de/toaster-sles12sp3-products
MAINTAINER Dario Maiocchi "dmaiocchi@suse.com"

ARG repo
ARG cert

RUN echo "$cert" > /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT.pem
RUN update-ca-certificates
RUN echo "$repo" > /etc/zypp/repos.d/susemanager:dockerbuild.repo
RUN zypper --non-interactive --gpg-auto-import-keys ref
RUN zypper --non-interactive in aaa_base aaa_base-extras net-tools timezone vim less sudo tar python python-xml
