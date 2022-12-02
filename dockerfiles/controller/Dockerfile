FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n install nmap tar gzip && \
    zypper clean -a
