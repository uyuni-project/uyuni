FROM opensuse/leap:15.2
MAINTAINER Ricardo Mateus <rdiasmateus@suse.com>

RUN export LC_ALL=en_US.UTF-8

RUN zypper ar https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ uyuni-server-stabl

RUN zypper --gpg-auto-import-keys --non-interactive in --auto-agree-with-licenses patterns-uyuni_server spacecmd

# add tomcat user to www group
RUN usermod -a -G www tomcat

# only for development
RUN zypper --gpg-auto-import-keys --non-interactive install vim less

# copy setup_environment
COPY setup_env.sh /root/
# change taskomatic start script to include defulat taskomatic properties
ADD taskomatic /usr/sbin/taskomatic
RUN chmod +x /usr/sbin/taskomatic

# PostgreSQL setup
ADD setup-db-postgres.sh /root/setup-db-postgres.sh
RUN sh /root/setup-db-postgres.sh
ADD postgresql.conf /var/lib/pgsql/data/postgresql.conf

# Apache
EXPOSE 80
EXPOSE 443
# Salt
EXPOSE 4505
EXPOSE 4506
# database
EXPOSE 5432/tcp

# check if those are needed
EXPOSE 67/udp
EXPOSE 69/udp
EXPOSE 5222/tcp
EXPOSE 5269/tcp

COPY entrypoint.sh /entrypoint.sh

CMD /entrypoint.sh
