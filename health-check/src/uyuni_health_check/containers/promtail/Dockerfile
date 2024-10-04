FROM opensuse/leap:latest

COPY promtail-linux-amd64 /usr/bin/promtail
RUN chmod a+x /usr/bin/promtail

ENTRYPOINT ["promtail"]
CMD ["--config.file=/etc/promtail/config.yml"]

