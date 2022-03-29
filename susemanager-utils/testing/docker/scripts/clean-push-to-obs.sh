#/bin/bash -e
echo "*************** CLEANING DIRECTORIES GENERATED AS USER ROOT  ***************"
rm -rf web/html/src/node_modules
rm -rf web/html/src/dist
rm -f web/node-modules.tar.gz
rm -rf rel-eng/custom/*
