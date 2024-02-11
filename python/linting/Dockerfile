#!BuildTag: uyuni-lint:latest
FROM opensuse/tumbleweed

ENV MOUNT="/mgr"

# Define labels according to https://en.opensuse.org/Building_derived_containers
LABEL org.opencontainers.image.title="Linting container for Uyuni"
LABEL org.opencontainers.image.description="This contains the tooling to format and lint Uyuni Python code"
LABEL org.opencontainers.image.version="1.0.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"

COPY ./pylintrc /root/.pylintrc

RUN zypper install -y python3 python3-black python3-pylint

VOLUME [ ${MOUNT} ]
WORKDIR $MOUNT

CMD ["/bin/bash"]

