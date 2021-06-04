FROM opensuse/uyuni/server/postgres

LABEL org.opencontainers.image.authors="Silvio Moioli <moio@suse.com>"

VOLUME /var/lib/pgsql/data

COPY entrypoint.sh /usr/local/bin/

CMD entrypoint.sh
