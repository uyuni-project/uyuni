
# Spacewalk Testsuite repository

* master
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* manager30
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* manager31
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)

# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/) and [Spacewalk](http://spacewalk.redhat.com/).
The testsuite is based on [Cucumber](https://cucumber.io/).

We recommend you to read about the [**basic concepts** of Cucumber that we will be using in this testsuite: features, scenarios and steps](https://cucumber.io/docs/reference).

## Running the testsuite locally

You can run the Spacewalk Testsuite [with sumaform (Official way)](docs/sumaform-howto.md).

### Development

[How-to-contribute](docs/howto.md)

## Tests order

Tests are grouped into *features*.
Features are executed sequentially from the top to the bottom of this file:

https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml

## Run just a single feature

Add the feature file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

# License

The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.

# Useful tutorials

* [Testing API tutorial](docs/api-call.md)
* [Branches that we use](docs/branches.md)
* [Debug](docs/Debug.md)
* [Pitfalls-test.md](docs/Pitfalls-test.md)
