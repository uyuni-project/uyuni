FROM opensuse/leap:15.4
# list of packages to installed are build dependencies from
# spacewalk-web package
RUN zypper ref -f && \ 
    zypper -n install \
      postgresql-server && \
    zypper clean -a
RUN mkdir /var/lib/pgsql/data && chown postgres /var/lib/pgsql/data
RUN su - postgres -c /usr/lib/postgresql14/bin/initdb
CMD su - postgres -c /usr/lib/postgresql14/bin/postgres 

