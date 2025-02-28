FROM opensuse/leap:latest
#FROM registry.suse.com/bci/python:3.11

COPY promtail-linux-amd64 /usr/bin/promtail
RUN chmod a+x /usr/bin/promtail

ENTRYPOINT ["promtail"]
CMD ["--config.file=/etc/promtail/config.yml"]