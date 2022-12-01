FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n install salt python3 && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    zypper clean -a
