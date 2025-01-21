FROM opensuse/leap:latest
#FROM registry.suse.com/bci/python:3.11

COPY promtail-linux-amd64 /usr/bin/promtail
RUN chmod a+x /usr/bin/promtail

RUN zypper -n in python311 python311-requests
COPY complete_checker.py /usr/bin/complete_checker.py

COPY run.py /usr/bin/run.py
RUN chmod +x /usr/bin/run.py

ENTRYPOINT ["/usr/bin/run.py"]