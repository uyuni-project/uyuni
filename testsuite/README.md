# SUSE Manager automated test suite

# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/).

Before you start, make sure you know about the [**basic concepts**](https://cucumber.io/docs/reference) of Cucumber that we are using in this testsuite: features, scenarios and steps.

Apart from Cucumber, the testsuite relies on a number of [software components](docs/software-components.md).


# Running the testsuite

You can run the SUSE Manager testsuite [with sumaform](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite).

## Core features, idempotency and tests order

The tests (features) included in the `[testsuite.yml](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml)` file will be executed sequentially from the top to the bottom.

[Idempotency](docs/idempotency.md) is the faculty to run same the feature any number of times. The basic idea of such a feature is that it does not change its environment.

The features are grouped by core and secondary features.

For the group of the **core features**, the order is relevant. The core features are by design not idempotent, and serve to create a basic testing environment.

The **secondary features** can be run any number of times, and the order is not important.

## Optional components

To know how to test with or without optional components like a proxy, a CentOS minion or a SSH minion, look at the [optional components instructions](docs/optional.md).

# Contributing

## Procedure

1. **Always** create a PR (even for backporting)
2. Your PR always needs at least one reviewer to approve

## Guidelines for coding

To get started, see the documentation about [Using and writing Cucumber steps](docs/cucumber-steps.md). It covers most common steps in an ordered manner, as well as the way to write new steps.

Please read with attention the [guidelines](docs/Guidelines.md). They cover style issues, idempotency concerns, file naming conventions, and features, scenarios and test naming conventions.

Check the [code coverage results](docs/codecoverage.md) after you have run the test suite with your code.

There are also hints about how to [debug the testsuite](docs/Debug.md) and about [Pitfalls in writing the testsuite](docs/Pitfalls-test.md).

# Branches used

* Development (to be run against a HEAD version of SUSE Manager):

  * [`Manager`](https://github.com/SUSE/spacewalk-testsuite-base)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):

  * [`Manager-3.1`](https://github.com/SUSE/spacewalk/tree/manager31)
  * [`Manager-3.0`](https://github.com/SUSE/spacewalk/tree/manager30)
