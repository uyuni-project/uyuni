
# Spacewalk Testsuite

[![Build Status SLEnkins branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=slenkins)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)


## Introduction

Testsuite to automatically test a Spacewalk installation

## Running

You can run the Spacewalk Testsuite basically with two options:

* with SLEnkins framework 
* static setup 

### Run Spacewalk Testsuite within SLEnkins Testing Framework

[SLEnkins documentation](docs/SLEnkins-howto.md)


### Static setup
* The SUSE Manager official testsuite applicance has all the gems pre-installed as rpms. Alternatively you can use [rbenv](http://rbenv.org/) (packages available [here](https://software.opensuse.org/download/package?project=devel:languages:ruby:extensions&package=rbenv))

```console
rbenv local $version
gem install bundler --pre
rbenv rehash
bundle install
```

Setup the following environment variables.

* TESTHOST environment variable can be passed to change the default server you are testing against.
* BROWSER (default `phantomjs` environment variable can be passed to change the default browser: `chrome`, `htmlunit`, `chrome`, `firefox`.
* Optionally, `ZAP_PROXY` to use [OWASP ZAP](https://code.google.com/p/zaproxy) to test for security vulnerabilities.

To run all standard tests call:

```console
rake
```

Or look at `rake -T` for available tasks.

## Custom feature run sets

Add a file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

## OWASP ZAP Support

If you set the `ZAP_PROXY` variable to localhost or `127.0.0.1`, the testsuite
will assume that the proxy runs on the same machine and it will take care
of starting/stopping it. It will assume ZAP is available at `/usr/share/owasp-zap/zap.sh`

If `ZAP_ACTIVE_ATTACK` is set, additionally an active attack will be performed on the
server and added to the results.

If a file `zap_ignored.txt` exists, those vulnerabilities will produce no failures.
A `zap_all.txt` file is generated after each run so that the initial zap_ignored.txt
can be fed in and maintained.

To run the tests with the security feature use the rake command below in addition 
to setting the appropriate environment variables:

```console
rake cucumber:security_test
```

## Conventions when adding more tests

* Add required gems to `Gemfile`.
* Unit tests in `test/testsuite_name`.
* Helpers for unit tests in `test/helper.rb`.
* Cucumber features under features.
* Helpers shared scross tests/features should go into the `lib/spacewalk_testsuite_base library`.

## License

* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.




