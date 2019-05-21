# Container used to test Content Management feature
#
# VERSION               1.0.0

FROM registry.mgr.suse.de/sles12sp2python
LABEL maintainer="mc@suse.com"

ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

ADD remove_repositories.sh /root/remove_repositories.sh
RUN /root/remove_repositories.sh

RUN rm /root/add_packages.sh
RUN rm /root/remove_repositories.sh

