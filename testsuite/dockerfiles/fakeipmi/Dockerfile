FROM opensuse/leap:15.6

RUN zypper -n in OpenIPMI && \
    mkdir -p /etc/ipmi

COPY ipmisim1.emu lan.conf fake_ipmi_host.sh /etc/ipmi

CMD ["ipmi_sim", "-n"]
