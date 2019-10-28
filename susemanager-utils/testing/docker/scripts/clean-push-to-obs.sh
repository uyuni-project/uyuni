#/bin/bash -e
echo "*************** CLEANING DIRECTORIES GENERATED AS USER ROOT  ***************"
rm -rf susemanager-frontend/susemanager-nodejs-sdk-devel/node_modules
rm -rf susemanager-frontend/node_modules
rm -rf web/html/src/node_modules
rm -rf web/html/src/dist
rm -f susemanager-frontend/susemanager-nodejs-sdk-devel/susemanager-nodejs-modules.tar.gz
rm -rf rel-eng/custom/*
