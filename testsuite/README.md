# SUSE Manager automated test suite

# Introduction

This is the automated testsuite for [SUSE Manager](https://www.suse.com/products/suse-manager/).

Before you start, make sure you know about the [**basic concepts**](https://cucumber.io/docs/reference) of Cucumber that we are using in this testsuite: features, scenarios and steps.

Apart from Cucumber, the testsuite relies on a number of [software components](documentation/software-components.md).


# Running the testsuite

You can run the SUSE Manager testsuite [with sumaform](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite).

If you want to run the testsuite for [Uyuni](https://www.uyuni-project.org), nothing special needs to be done. The testuite will autodetect it.

## Core features, idempotency and tests order

The tests (features) included in the `[testsuite.yml](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/run_sets/testsuite.yml)` file will be executed sequentially from the top to the bottom.

[Idempotency](documentation/idempotency.md) is the faculty to run same the feature any number of times. The basic idea of such a feature is that it does not change its environment.

The features are grouped by core and secondary features.

For the group of the **core features**, the order is relevant. The core features are by design not idempotent, and serve to create a basic testing environment.

The **secondary features** can be run any number of times, and the order is not important.

## Optional components

To know how to test with or without optional components like a proxy, a CentOS minion or a SSH minion, look at the [optional components instructions](documentation/optional.md).


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

  * [`Manager`](https://github.com/SUSE/spacewalk-testsuite-base)

* Release (to be run against a nightly or released *tagged* version of SUSE Manager):

  * [`Manager-3.1`](https://github.com/SUSE/spacewalk/tree/manager31)
  * [`Manager-3.0`](https://github.com/SUSE/spacewalk/tree/manager30)


# Dummy packages used by the Testsuite
Some of the scenarios that are tested on this testsuite make use of some external testing repositories which contain dummy packages. These packages are used to test package and patch installation and upgrade.

The repositories are:
- https://build.suse.de/project/show/Devel:Galaxy:BuildRepo - for packages which must be installed on the client systems already;
- https://build.suse.de/project/show/Devel:Galaxy:TestsuiteRepo - provides packages for "Test-Channel-x86_64" channel;
- https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Ubuntu-Test - already installed Ubuntu packages;
- https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Ubuntu-Test-Updates - provides packages for "Test-Channel-Deb-AMD64" channel.

## Type of provided packages
- Normal dummy packages: `andromeda-dummy`, `hoag-dummy`, `orion-dummy`, `milkyway-dummy`, etc.
- Wrong encoding of RPM attributes `blackhole-dummy` package: This package should be successfully imported and you will see it available as part of "Test-Channel-x86_64" if reposync handled the encoding correctly.
