# Container used to test java and python code of SUSE Manager against postgresql
#
# VERSION               4.0.0

FROM registry.mgr.suse.de/suma-head-base:latest
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh

ADD rhn.conf /root/rhn.conf

RUN sh /root/setup-db-postgres.sh

ADD postgresql.conf /var/lib/pgsql/data/postgresql.conf

