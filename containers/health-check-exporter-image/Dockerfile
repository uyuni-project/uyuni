#!BuildTag: health-check-exporter

ARG BASE=registry.suse.com/bci/bci-base:15.6

FROM $BASE

RUN zypper -n ref
RUN zypper -n install python311-PyYAML python311-pip python311-prometheus-client python311-Jinja2 python311-rich

WORKDIR /opt/
ENTRYPOINT ["python3.11", "/opt/supportconfig_exporter.py"]

