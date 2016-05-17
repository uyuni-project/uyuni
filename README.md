
# Spacewalk Testsuite

[![Build Status](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)

## Introduction

Testsuite to automatically test a Spacewalk installation

## Running

* Get requirements from rpms or

```
rvm use $version
gem install bundler --pre
bundle install
```

* TESTHOST environment variable can be passed to change the default
  server.
* BROWSER environment variable can be passed to change the default
  browser: chrome, htmlunit, chrome, firefox
* ZAP_PROXY to use OWASP ZAP to test for security vulnerabilities
  (https://code.google.com/p/zaproxy)

To run all standard tests call:

rake

Or look at rake -T

## Custom feature run sets

Add a file into run_sets/$name.yml and then execute rake cucumber:$name

## OWASP ZAP Support

If you set the ZAP_PROXY variable to localhost or 127.0.0.1, the testsuite
will assume that the proxy runs on the same machine and it will take care
of starting/stopping it. It will assume ZAP is available at /usr/share/owasp-zap/zap.sh

If ZAP_ACTIVE_ATTACK is set, additionally an active attack will be performed on the
server and added to the results.

If a file zap_ignored.txt exists, those vulnerabilities will produce no failures.
A zap_all.txt file is generated after each run so that the initial zap_ignored.txt
can be fed in and maintained.

To run the tests with the security feature use the rake command below in addition 
to setting the appropriate environment variables:

rake cucumber:security_test

## Conventions when adding more tests

* Add required gems to Gemfile
* Unit tests in test/testsuite_name
* Helpers for unit tests in test/helper.rb
* Cucumber features under features
* Helpers shared scross tests/features should go into the
  lib/spacewalk_testsuite_base library

## License

The testsuite is licensed under the MIT license. See the MIT-LICENSE.txt
file included in the distribution.




