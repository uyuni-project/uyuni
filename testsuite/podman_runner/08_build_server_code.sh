#!/bin/bash
set -xe
export CR=docker
${CR} exec uyuni-server-all-in-one-test bash -c "cd /java && ant -f manager-build.xml ivy refresh-branding-jar deploy-local"
${CR} exec uyuni-server-all-in-one-test bash -c "cd /web/html/src;[ -d dist ] || mkdir dist;yarn install --force --ignore-optional --production=true --frozen-lockfile;yarn autoclean --force;yarn build:novalidate; rsync -a dist/ /srv/www/htdocs/"

