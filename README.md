# SUSE Manager (and spacewalk) Cucumber Test suite

# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/) and [Spacewalk](http://spacewalk.redhat.com/).
The testsuite is based on [Cucumber](https://cucumber.io/).

We recommend you to read about the [**basic concepts** of Cucumber that we will be using in this testsuite: features, scenarios and steps](https://cucumber.io/docs/reference).

# Branch used

* Development (to be run against a HEAD version of SUSE Manager):

  * [`master`](https://github.com/SUSE/spacewalk-testsuite-base)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):
  * [`manager30`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager30)
  * [`manager31`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager31)

All other branches are considered legacy (not under development anymore): `Manager 2.1`, `manager21-longterm`, `Manager 17`, `Manager 12`.

# Running the testsuite

You can run the Spacewalk Testsuite [with sumaform](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite).

## Tests order

Tests are grouped into *features*.
[**Standard testsuite features Features included in the `testsuite.yml` file will be executed sequentially from the top to the bottom**](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml).

## Run just a single run set

You can override the tests that are executed by adding your run set:
1. Login into the `controller` host.
2. Add the feature(s) you would like to run into `run_sets/$name.yml`
3. Call Cucumber to execute your custom run set (`rake cucumber:$name`)

# Contributing

## Procedure

1. Fork it
2. Create your branch containing the new tests, starting from master (`git checkout master && git checkout -b my-new-tests`)
3. Commit your changes (`git commit -am 'Add some tests'`)
4. Push to the branch (`git push origin my-new-tests`)
5. Create new Pull Request
6. **IMPORTANT**: Create a Pull Request to backport your new tests to the other branches used in this project.

## Guidelines

* Steps should be grouped **by topic** and not by feature.

Steps files should be like the following:

    Creating users
    Navigation
    Running commands
    Salt

* Reuse steps, don't create new if you don't need them (look under `./features/step_definitions/` to see which steps are already implemented)

* [Check the code coverage results](docs/codecoverage.md), after you have run the test suite with your code.

## Linting

Automatic linting via Rubocop in Travis:

* `master`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager30`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager31`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)

# License

The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.

# Useful tutorials

* [Testing API tutorial](docs/api-call.md)
* [Branches that we use](docs/branches.md)
* [Debug](docs/Debug.md)
* [Pitfalls-test.md](docs/Pitfalls-test.md)
