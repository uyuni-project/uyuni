FROM opensuse/leap:15.3

LABEL org.opencontainers.image.authors="Silvio Moioli <moio@suse.com>"

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --no-recommends postgresql-server

VOLUME /var/lib/pgsql/data

# https://github.com/docker-library/postgres/blob/aed4d450b287b8fb3e834e21df8eeee37e0f8d28/13/Dockerfile#L189
STOPSIGNAL SIGINT

EXPOSE 5432/tcp

COPY entrypoint.sh /usr/local/bin/

CMD entrypoint.sh
