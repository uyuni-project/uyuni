# Container used to test java and python code of SUSE Manager against Oracle database
#
# VERSION               0.1

FROM suma-head-base
MAINTAINER Flavio Castelli "fcastelli@suse.com"

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

ADD tnsnames.ora /etc/tnsnames.ora
ADD rhn.conf /etc/rhn/rhn.conf
ADD rhn.conf /root/rhn.conf
