
# Spacewalk Testsuite

Master
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
Manager 30
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)


## Introduction

Welcome ! This is the Testsuite to automatically test Suse-Manager/Spacewalk.


## How to Contribute. 

#### CREATE always a PR, first against MASTER. After that, create a PR for other branches, if needed for backport the feature.

#### 0) Make a good commit message: 
 https://gist.github.com/matthewhudson/1475276
It is description by Linus Torvalds about how a good commit message should look like.
Of course it needs to be taken with "salt", there is no need for a paragraph on every commit, but
a good headline is always a must, and sometimes taking the time to explain design decisions and
anything in the head of the committer in the rest of the paragraph is still a good idea.

#### 0 b) Avoid extra commits.

Use ```git rebase -i``` locally

If you commit to save temp work and be able to revert, before you "git push" (because you can't do rebase afterwards) do a git interactive rebase and reorder, swap, and merge various commits into one. They should end as well-described units of work that are independent from each other. If a bugfix requires two commits and it is not usable without both, then it is a good idea to make the two commits a single one, unless one of the fixes makes sense as its own.


#### 1) Steps should be grouped **by topic** and not by feature.

Steps files should be like:

    Creating users
    Navigation
    Running commands
    Salt
    
#### 2) Reuse steps, don't create new if you don't need them.
        

#### 3) Check the code coverage results, after you runned the testsuite with your code.

[Howto check code coverage](docs/codecoverage.md)

### Usefull tutorial infos:

[Testing-api tutorial](docs/api-call.md), [Branches that we use](docs/branches.md)

[Debug](docs/Debug.md), [Pitfalls-test.md](docs/Pitfalls-test.md)

## Running

You can run the Spacewalk Testsuite:

* [with sumaform (Official way)](docs/sumaform-howto.md)


## Tests order: 
The tests are ordered and executed with the order of this yaml file:

https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml

## Custom feature run sets

Add a file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

At moment we support only one, this is the refhost (smoke tests set)


### RE/Run single feature

On the control-node modify the ``vi /usr/bin/run-testsuite``. The file has per default already the needed hostname set.
```console
#!/bin/bash

export SSHMINION=..
export CENTOSMINION=your.minion.tf.local
export TESTHOST=..
export CLIENT=..
export MINION=..
export BROWSER=phantomjs

cd /root/spacewalk-testsuite-base
cucumber features/MY_FEATURE_NAME
```

* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.



