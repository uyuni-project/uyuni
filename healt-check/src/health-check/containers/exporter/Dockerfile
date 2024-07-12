FROM registry.suse.com/bci/python:3.6

LABEL maintainer Pablo Suárez Hernández <psuarezhernandez@suse.com>

COPY requirements.txt /opt/
RUN zypper -n ref
RUN zypper -n install python3-PyYAML python3-pip
RUN pip3.6 install -r /opt/requirements.txt

COPY supportconfig_exporter.py /opt/
COPY config.yml /opt/

WORKDIR /opt/
ENTRYPOINT python3.6 /opt/supportconfig_exporter.py
