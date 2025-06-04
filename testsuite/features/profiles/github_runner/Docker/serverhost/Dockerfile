# Container used to test Content Management feature
#
# VERSION               1.0.0

FROM localhost:5002/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite

ARG repo
ARG cert

RUN echo "$cert" > /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT.pem
RUN update-ca-certificates
RUN echo "$repo" > /etc/zypp/repos.d/susemanager:dockerbuild.repo

ADD nsswitch.conf /etc/nsswitch.conf
ADD avahi-daemon.conf /root/avahi-daemon.conf

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh
