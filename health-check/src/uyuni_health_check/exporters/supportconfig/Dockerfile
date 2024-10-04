FROM registry.suse.com/bci/python:3.6

LABEL maintainer Pablo Suárez Hernández <psuarezhernandez@suse.com>

COPY requirements.txt /opt/
RUN zypper -n ref
RUN zypper -n install python3-PyYAML python3-pip
RUN pip3.6 install -r /opt/requirements.txt

COPY supportconfig_exporter.py /opt/
COPY config.yaml /opt/config.yml
#ARG CONFIG="supportconfig_path: /home/ygutierrez/src/uyuni/healt-check/src/scc_susemgr902_240612_0943\nport: 9000\nscrape_frequency: 60"
#RUN echo ${CONFIG}
#RUN touch /opt/config.yml
#RUN echo ${CONFIG} > /opt/config.yml

WORKDIR /opt/
ENTRYPOINT python3.6 /opt/supportconfig_exporter.py
