
# Spacewalk Testsuite

[![Build Status](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)

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
rbenv use $version
gem install bundler --pre
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

## Conventions when adding more tests

* Add required gems to `Gemfile`.
* Unit tests in `test/testsuite_name`.
* Helpers for unit tests in `test/helper.rb`.
* Cucumber features under features.
* Helpers shared scross tests/features should go into the `lib/spacewalk_testsuite_base library`.

## License

* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.




