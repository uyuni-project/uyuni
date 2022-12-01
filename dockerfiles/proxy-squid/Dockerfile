FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n install squid python3-PyYAML && \
    zypper clean -a
RUN mkdir /run/squid
CMD squid -N
