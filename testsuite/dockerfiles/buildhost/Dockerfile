FROM opensuse/leap:15.5
RUN zypper ref -f && \ 
    zypper -n install docker docker-buildx \
      && \
    zypper clean -a
CMD dockerd

