FROM ghcr.io/jordimassaguerpla/uyuni/server-httpd-dev:master
#TODO
#COPY files
#RUN call make build and make install to build nodejs. See spacewalk-web
RUN echo "test" > /srv/www/htdocs/test.html
CMD /usr/sbin/start_apache2 -DFOREGROUND -k start
