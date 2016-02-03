#!/usr/bin/env bash
export PATH=$PATH:$(readlink -f ./node_modules/.bin/)

#browserify -r react > ../javascript/manager/react-bundle.js
browserify -x react -t [ babelify --presets [ es2015 react ] ] manager/org-state-catalog.js > ../javascript/manager/org-state-catalog-app.js