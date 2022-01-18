FROM opensuse/leap:15.2

# Build and push to registry.mgr.suse.de/uyuni-push-to-obs:latest
# docker build -t uyuni-push-to-obs .
# docker tag uyuni-push-to-obs registry.mgr.suse.de/uyuni-push-to-obs:latest
# docker tag uyuni-push-to-obs registry.mgr.suse.de/uyuni-push-to-obs:VERSION <- See the version label above
# docker push registry.mgr.suse.de/uyuni-push-to-obs:latest
# docker push registry.mgr.suse.de/uyuni-push-to-obs:VERSION

LABEL org.opencontainers.image.description="Container used to push packages to obs by CI for SUSE Manager 4.1"
LABEL org.opencontainers.image.title="SUSE Manager 4.1 push-to-obs container"
LABEL org.opencontainers.image.url="https://github.com/SUSE/spacewalk/blob/Manager-4.1/susemanager-utils/testing/docker/4.1/uyuni-push-to-obs/Dockerfile"
LABEL org.opencontainers.image.version=4.1.3
LABEL org.opencontainers.image.authors="Julio Gonzalez <jgonzalez@suse.com>, Jordi Massaguer Pla <jmassaguerpla@suse.com>"
LABEL org.opencontainers.image.vendor="Galaxy Team"

# Add the repositories
ADD add_repositories.sh /root/add_repositories.sh
RUN /root/add_repositories.sh

RUN zypper --non-interactive --gpg-auto-import-keys ref

# Install the packages
ADD add_packages.sh /root/add_packages.sh
RUN /root/add_packages.sh

# Install yarn
ADD add_yarn.sh /root/add_yarn.sh
RUN /root/add_yarn.sh
