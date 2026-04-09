FROM opensuse/leap:15.6@sha256:b084d6e29d975ce9123fd52bd201ac020628797f2772e9171ef76f33cc92d591

RUN zypper -n in OpenIPMI && \
    mkdir -p /etc/ipmi

COPY ipmisim1.emu lan.conf fake_ipmi_host.sh /etc/ipmi

CMD ["ipmi_sim", "-n"]
