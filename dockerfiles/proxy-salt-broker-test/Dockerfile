FROM ghcr.io/jordimassaguerpla/uyuni/proxy-salt-broker-dev:master
COPY proxy/proxy/salt-broker/broker /etc/salt/
COPY proxy/proxy/salt-broker/salt-broker /usr/local/bin/salt-broker
#CMD /usr/local/bin/salt-broker
CMD python -m http.server
