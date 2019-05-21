# Container used to test java and python code of SUSE Manager against postgresql
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/suma-3.0-ora:latest
LABEL maintainer="mc@suse.com"

#RUN zypper --non-interactive in -n spacewalk-admin susemanager-schema smdba
RUN zypper --non-interactive in -n spacewalk-admin smdba

ADD susemanager-schema-3.0.14-6.1.noarch.rpm /root/susemanager-schema-3.0.14-6.1.noarch.rpm

RUN zypper --non-interactive in /root/susemanager-schema-3.0.14-6.1.noarch.rpm

RUN /usr/bin/rhn-config-schema.pl --source=/etc/sysconfig/rhn/oracle/main.sql --target=/etc/sysconfig/rhn/oracle/deploy.sql --tablespace-name=data_tbs

RUN cp /root/rhn.conf /etc/rhn/

RUN /etc/init.d/oracle start

ADD docker-oracle-db4eclipse.sh /root/docker-oracle-db4eclipse.sh

ADD db-prepare.sh /root/db-prepare.sh

#CMD /bin/sh /root/docker-oracle-db4eclipse.sh

