
# Spacewalk Testsuite repository

* master
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* manager30
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* manager31
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)

# Introduction

Welcome! This is the Testsuite to automatically test Suse-Manager/Spacewalk.

# Running testsuite locally

You can run the Spacewalk Testsuite [with sumaform (Official way)](docs/sumaform-howto.md).

# Tests order

Tests are grouped into *features*.
Features are executed sequentially from the top to the bottom of this file:

https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml

## Custom feature run sets

Add a file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

At moment we support only one, this is the refhost (smoke tests set)
* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.

# Useful tutorial infos

* [Testing API tutorial](docs/api-call.md)
* [Branches that we use](docs/branches.md)
* [Debug](docs/Debug.md)
* [Pitfalls-test.md](docs/Pitfalls-test.md)

# Contribution 

[How-to-contribute](docs/howto.md)



