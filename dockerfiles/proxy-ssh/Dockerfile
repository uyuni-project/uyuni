FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n install openssh-server && \
    zypper clean -a
CMD ssh-keygen -A && /usr/sbin/sshd -De
