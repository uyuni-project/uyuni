# Container used to test java and python code of SUSE Manager against postgresql
#
# VERSION               3.1.0

FROM suma-3.1-ora-4eclipse-pre
LABEL maintainer="mc@suse.com"

ADD docker-oracle-db4eclipse.sh /root/docker-oracle-db4eclipse.sh

CMD /bin/sh -x /root/docker-oracle-db4eclipse.sh

