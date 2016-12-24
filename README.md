
# Spacewalk Testsuite

Master
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
Manager 30
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)


## Introduction

Testsuite to automatically test Spacewalk/Suse-Manager.

### Usefull tutorial infos:

[Testing-api tutorial](docs/api-call.md), [Branches that we use](docs/branches.md)

[Debug](docs/Debug.md), [Pitfalls-test.md](docs/Pitfalls)

## Running

You can run the Spacewalk Testsuite:

* [with sumaform (Official way)](docs/sumaform-howto.md)

* [Static run howto](docs/static-run.md)


## Custom feature run sets

Add a file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

At moment we support only one, this is the refhost (smoke tests set)

## How to Contribute, develop new tests.

#### CREATE a PR, always, against MASTER. After that, create a PR for other branches, if needed.

#### 1) Steps should be grouped by topic and not by feature.

Steps files should be like:

    Creating users
    Navigation
    Running commands
    Salt
    
#### 2) Reuse steps, don't create new if you don't need.
        
* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.




