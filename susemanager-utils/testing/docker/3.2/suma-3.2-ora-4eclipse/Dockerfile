# Container used to test java and python code of SUSE Manager against postgresql
#
# VERSION               3.2.0

#FROM registry.mgr.suse.de/suma-3.2-ora-4eclipse-pre
#use the local as we did not push the "pre"
FROM suma-3.2-ora-4eclipse-pre
LABEL maintainer="mc@suse.com"

ADD docker-oracle-db4eclipse.sh /root/docker-oracle-db4eclipse.sh

CMD /bin/sh -x /root/docker-oracle-db4eclipse.sh

