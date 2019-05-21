# Container used to test java and python code of SUSE Manager against postgresql
#
# VERSION               3.1.0

FROM registry.mgr.suse.de/suma-3.1-base:latest
LABEL maintainer="mc@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh

ADD rhn.conf /root/rhn.conf

RUN sh /root/setup-db-postgres.sh

ADD postgresql.conf /var/lib/pgsql/data/postgresql.conf

