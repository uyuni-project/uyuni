FROM registry.suse.com/bci/python:3.11

LABEL maintainer Pablo Suárez Hernández <psuarezhernandez@suse.com>

COPY requirements.txt /opt/
RUN zypper -n ref
RUN zypper -n install python311-PyYAML python311-pip
RUN pip3.11 install -r /opt/requirements.txt

WORKDIR /opt/
ENTRYPOINT ["python3.11", "/opt/supportconfig_exporter.py"]
