#!/bin/bash
set -ex

AUTH_REGISTRY_USER=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d'|' -f1)
AUTH_REGISTRY_PASSWD=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d'|' -f2)

sudo docker pull "ghcr.io/$UYUNI_PROJECT/uyuni/opensuse/leap/15.6:$UYUNI_VERSION"
sudo docker pull "ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:$UYUNI_VERSION"

sudo docker tag "ghcr.io/$UYUNI_PROJECT/uyuni/opensuse/leap/15.6:$UYUNI_VERSION" localhost:5002/opensuse/leap:15.6
sudo docker push localhost:5002/opensuse/leap:15.6

sudo docker tag "ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:$UYUNI_VERSION" localhost:5002/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
sudo docker push localhost:5002/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite

sudo docker login -u "${AUTH_REGISTRY_USER}" -p "${AUTH_REGISTRY_PASSWD}" localhost:5001
sudo docker tag "ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:$UYUNI_VERSION" localhost:5001/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
sudo docker push localhost:5001/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
