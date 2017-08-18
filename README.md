# SUSE Manager automated test suite

* `master`
[![Build Status Master branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=master)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager31`
[![Build Status Manager31 branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager31)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)
* `manager30`
[![Build Status Manager30 branch](https://travis-ci.org/SUSE/spacewalk-testsuite-base.svg?branch=manager30)](https://travis-ci.org/SUSE/spacewalk-testsuite-base)


# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/).

Before you start, make sure you know about the [**basic concepts**](https://cucumber.io/docs/reference) of Cucumber that we are using in this testsuite: features, scenarios and steps.

Apart from Cucumber, testsuite relies on a number of [software components](docs/software-components.md).


# Running the testsuite

You can run the Spacewalk Testsuite [with sumaform](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite).

## Core features, idempotency and tests order

The tests (features) included in the `[testsuite.yml](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml)` file will be executed sequentially from the top to the bottom.

The features are grouped by core and secondary features.

[Idempotency](docs/idempotency.md) is the faculty to run same the feature any number of times. The basic idea of such a feature is that it does not change its environment.

For the group of the **core features**, the order is relevant. The core features are by design not idempotent, and serve to create a basic testing environment.

The **secondary features** can be run any number of times, and the order is not important.

## Images used by the testsuite

The images are built by kiwi: http://download.suse.de/ibs/Devel:/Galaxy:/Terraform:/Images/, and sumaform deploys them.


# Contributing

## Procedure

1.  **Always** create a PR (even for backporting)
2. Your PR always needs at least one reviewer to approve

## Guidelines for coding:

1.  **Always** create an idempotent feature, with right prefixes, and **always** as a secondary feature
   -> If the feature is not idempotent, it will be not merged upstream.
2. Scenario that cleans up should be named : ```Scenario: CLEANUP: remove xx pkg""
   or scenario before the actual test should named "Scanario: PRE-Requirement: install that"
3. Steps should be grouped **by topic** and not by feature.
4. Use the right prefix for your feature name. See [here](run_sets/testsuite.yml)
5. If you do operations on packages, look [here](docs/Patches_test.md)
6. Reuse steps, don't create new ones if you don't need them (look under `./features/step_definitions/` to see which steps are already implemented)
7. Check the [code coverage results](docs/codecoverage.md) after you have run the test suite with your code.


# Branches used

* Development (to be run against a HEAD version of SUSE Manager):

  * [`master`](https://github.com/SUSE/spacewalk-testsuite-base)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):
  * [`manager30`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager30)
  * [`manager31`](https://github.com/SUSE/spacewalk-testsuite-base/tree/manager31)

All other branches are considered legacy (not under development anymore): `Manager 2.1`, `manager21-longterm`, `Manager 17`, `Manager 12`.


# Useful tutorials

* [Testing API tutorial](docs/api-call.md)
* [Debug](docs/Debug.md)
* [Pitfalls](docs/Pitfalls-test.md)
