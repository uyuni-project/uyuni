# Container used to test java code of SUSE Manager
#
# VERSION               1.0.2

FROM registry.mgr.suse.de/suma-2.1-base:1.0.0
LABEL maintainer="mc@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh
RUN sh /root/setup-db-postgres.sh

# Change the number of maximum connections allowed by PostgreSQL.
# This is by default 100, a value which is way too high compared to the actual
# needs of the test suites. A high value causes more memory to be requested by
# PostgreSQL on startup, leading to memory issues when two pgsql containers are
# are executed at the same time.
RUN sed -i 's/^max_connections =.*$/max_connections = 20/g' /var/lib/pgsql/data/postgresql.conf

ADD rhn.conf /root/rhn.conf

