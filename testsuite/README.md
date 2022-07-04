# Uyuni automated test suite

# Introduction

This is the automated testsuite for [Uyuni](https://www.uyuni-project.org/).

Before you start, make sure you know about the [**basic concepts**](https://cucumber.io/docs/gherkin/reference) of Cucumber that we are using in this testsuite: features, scenarios and steps.

Apart from Cucumber, the testsuite relies on a number of [software components](documentation/software-components.md).


# Running the testsuite

You can run the Uyuni testsuite [with sumaform](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md#running-the-testsuite).


## Core features, idempotency and tests order

The tests (features) included in the `[testsuite.yml](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml)` file will be executed sequentially from the top to the bottom.

[Idempotency](documentation/idempotency.md) is the faculty to run same the feature any number of times. The basic idea of such a feature is that it does not change its environment.

The features are grouped by core and secondary features.

For the group of the **core features**, the order is relevant. The core features are by design not idempotent, and serve to create a basic testing environment.

The **secondary features** can be run any number of times, and the order is not important.

## Optional components

To know how to test with or without optional components like a proxy, a RedHat-like minion or a SSH minion, look at the [optional components instructions](documentation/optional.md).


# Contributing

## Procedure

1. **Always** create a PR (even for backporting)
2. Your PR always needs at least one reviewer to approve

## Guidelines for coding

To get started, see the documentation about [Using and writing Cucumber steps](documentation/cucumber-steps.md). It covers most common steps in an ordered manner, as well as the way to write new steps.

Please read with attention the [guidelines](documentation/guidelines.md). They cover style issues, idempotency concerns, file naming conventions, and features, scenarios and test naming conventions.

Check the [code coverage results](documentation/codecoverage.md) after you have run the test suite with your code.

There are also hints about [Pitfalls in writing the testsuite](documentation/pitfalls.md).


# Branches used

* Development (to be run against a HEAD version of SUSE Manager):

  * [`master`](https://github.com/uyuni-project/uyuni)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):

  * [`Manager-4.1`](https://github.com/SUSE/spacewalk/tree/Manager-4.1)
  * [`Manager-4.2`](https://github.com/SUSE/spacewalk/tree/Manager-4.2)


# Dummy packages used by the Testsuite
Some of the scenarios that are tested on this testsuite make use of some external testing repositories which contain dummy packages. These packages are used to test package and patch installation and upgrade.

The Open Build System projects are:
- https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Pool - packages which must be installed on the client systems already;
- https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Updates - packages for "Test-Channel-x86_64" and "Test-Channel-Deb-AMD64" channels.

## Type of provided packages
- Normal dummy packages: `andromeda-dummy`, `hoag-dummy`, `orion-dummy`, `milkyway-dummy`, etc.
- Wrong encoding of RPM attributes: `blackhole-dummy` package. This package should be successfully imported and you will see it available as part of "Test-Channel-x86_64" if reposync handled the encoding correctly.
