FROM registry.suse.com/bci/bci-minimal:15.4
COPY sessioncleanup .
RUN chmod u+x /sessioncleanup
ENTRYPOINT ["/sessioncleanup"]
