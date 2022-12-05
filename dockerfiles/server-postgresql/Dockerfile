FROM opensuse/leap:15.4
# list of packages to installed are build dependencies from
# spacewalk-web package
RUN zypper ref -f && \ 
    zypper -n install \
      postgresql && \
    zypper clean -a
CMD /usr/lib/postgresql14/bin/postgres 

