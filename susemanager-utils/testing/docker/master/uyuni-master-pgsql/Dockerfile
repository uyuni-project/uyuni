# Container used to test java and python code of Uyuni against postgresql
#
# VERSION                4.0.0

FROM registry.mgr.suse.de/uyuni-master-base:latest
MAINTAINER Michael Calmer "Michael.Calmer@suse.com"

# Install the required packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh

ADD rhn.conf /root/rhn.conf

RUN sh /root/setup-db-postgres.sh

ADD postgresql.conf /var/lib/pgsql/data/postgresql.conf

RUN zypper in -y python3-pip

RUN pip3 install --upgrade pylint==1.8 astroid==1.6.5

