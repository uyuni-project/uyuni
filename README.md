# spacewalk and SUSE Manager automated test suite

* `master`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager30`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager31`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)


# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/) and [Spacewalk](http://spacewalk.redhat.com/).

Read more about the [**basic concepts** of Cucumber that we will be using in this testsuite: features, scenarios and steps](https://cucumber.io/docs/reference).

# Running the testsuite

You can run the Spacewalk Testsuite [with sumaform](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite).

## Core-Features, Idempotency and tests-order.

The tests (features) are grouped by: core-features and secondary (idempotent) features.
For the group of the **core-feature**, the order is relevant. The core-features are by design not idempotent, and serve to create a basic testing env.
The **secondary features** can be run XX number of times, and the order is irrelevant.
All new features should be **secondary features**, so you need to write cleanup steps on each feature.
[idempotency](docs/idempotency.md)

[**Standard testsuite features Features included in the `testsuite.yml` file will be executed sequentially from the top to the bottom**](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml).


## Images used on the testsuite.

The images are build by kiwi : http://download.suse.de/ibs/Devel:/Galaxy:/Terraform:/Images/, and sumaform deploy them.

# Contributing

## Procedure

1. Create **always** PR. (even for backporting)
2. Your PR needs always at least one Reviewer that approves

## Guidelines for coding:

1. Create **always** idempotent feature, with right prefixes. (and **always** Secondary feature)
   -> If the feature is not idempotent, it will be not merged upstream.
2. Scenario that cleanup, should be named : ```Scenario: CLEANUP: remove xx pkg""
   or scenario before the actual test, should named"Scanario: PRE-Requirement: install that"
3. Steps should be grouped **by topic** and not by feature.
4. Use the right prefix for your feature name. See [here](run_sets/testsuite.yml)
5. If you do pkg operation, look [here](docs/Patches_test.md)
6. Reuse steps, don't create new if you don't need them (look under `./features/step_definitions/` to see which steps are already implemented)
7. [Check the code coverage results](docs/codecoverage.md), after you have run the test suite with your code.


# Branch used

* Development (to be run against a HEAD version of SUSE Manager):

  * [`master`](https://github.com/SUSE/spacewalk-testsuite-base)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):
  * [`manager30`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager30)
  * [`manager31`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager31)

All other branches are considered legacy (not under development anymore): `Manager 2.1`, `manager21-longterm`, `Manager 17`, `Manager 12`.

# Useful tutorials

* [Testing API tutorial](docs/api-call.md)
* [Debug](docs/Debug.md)
* [Pitfalls-test.md](docs/Pitfalls-test.md)
