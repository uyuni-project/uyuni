FROM docker.io/cabelo/opensuse-leap-15.2
MAINTAINER Ricardo Mateus <rdiasmateus@suse.com>

COPY uyuni.key /tmp/uyuni.key

RUN rpm --import /tmp/uyuni.key
RUN zypper ar https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ uyuni-server-stabl && \
zypper ref && \
zypper --non-interactive in --auto-agree-with-licenses patterns-uyuni_server

EXPOSE 67/udp
EXPOSE 69/udp
EXPOSE 443/tcp
EXPOSE 4505/tcp
EXPOSE 4506/tcp
EXPOSE 5222/tcp
EXPOSE 5269/tcp
EXPOSE 5432/tcp

RUN usermod -a -G www tomcat

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh
RUN sh /root/setup-db-postgres.sh
ADD postgresql.conf /var/lib/pgsql/data/postgresql.conf

COPY setup_env.sh /root/

#COPY migration.sh /usr/lib/susemanager/bin/migration.sh
#COPY --chown=tomcat:tomcat server.xml /tmp/server.xml
#COPY migration.sh /usr/lib/susemanager/bin/migration.sh
COPY entrypoint.sh /entrypoint.sh

VOLUME ["/var/spacewalk"]
VOLUME ["/srv"]
VOLUME ["/var/lib/pgsql"]
VOLUME ["/var/cache"]


CMD /entrypoint.sh
