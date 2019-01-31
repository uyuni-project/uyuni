# Container used to test java and python code of Uyuni against postgresql
#
# VERSION                4.0.0

FROM registry.mgr.suse.de/uyuni-master-pgsql:latest
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

#RUN zypper --non-interactive in -n spacewalk-admin susemanager-schema smdba
RUN zypper --non-interactive in -n spacewalk-admin smdba

RUN zypper --non-interactive in /root/susemanager-schema-4.0.1-1.2.noarch.rpm

RUN /usr/bin/rhn-config-schema.pl --source=/etc/sysconfig/rhn/postgres/main.sql --target=/etc/sysconfig/rhn/postgres/deploy.sql --tablespace-name=None

RUN cp /root/rhn.conf /etc/rhn/

RUN smdba system-check autotuning --max_connections=50

RUN echo "host all  all    0.0.0.0/0  md5" >> /var/lib/pgsql/data/pg_hba.conf

RUN su - postgres -c "/usr/lib/postgresql10/bin/pg_ctl start" && su - postgres -c 'psql -d susemanager -v ON_ERROR_STOP=ON -f /etc/sysconfig/rhn/postgres/deploy.sql'

CMD /bin/sh /manager/susemanager-utils/testing/docker/scripts/init-pgsql-db4eclipse.sh

