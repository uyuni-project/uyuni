FROM opensuse/leap:15.4
RUN zypper ref -f && \ 
    zypper -n install python3-PyYAML apache2-mod_wsgi-python3 &&\
    zypper clean -a
CMD /usr/sbin/start_apache2 -DFOREGROUND -k start
 
