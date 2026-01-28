# Uyuni automated test suite

## Introduction

This is the automated test suite for [Uyuni](https://www.uyuni-project.org/).

Before you start, make sure you are familiar with the [**basic concepts**](https://cucumber.io/docs/gherkin/reference)
of Cucumber that we are using in our test suite: features, scenarios and steps.

Apart from Cucumber, the test suite relies on a number of [software components](documentation/software-components.md).

## Running the test suite

You can run the Uyuni test suite with [sumaform](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md#running-the-testsuite).

## Core features, idempotency and tests order

The tests (features) mentioned in the YAML files inside the [run_sets](https://github.com/uyuni-project/uyuni/tree/master/testsuite/run_sets)
folder will be executed sequentially the [following way](https://github.com/SUSE/susemanager-ci/blob/master/jenkins_pipelines/environments/common/pipeline.groovy#L100):

- sanity_check
- core
- reposync
- init_clients
- secondary
- secondary_parallelizable
- finishing

[Idempotency](documentation/idempotency.md) is the faculty to run the same feature any number of times. The basic idea
of such a feature is that it does not change its environment.

The features are grouped by core and secondary features.

- **Core features:** the order is relevant and they are by design not idempotent, and serve to create a basic testing
environment.
- **Secondary features:** the order is not important and they can be run any number of times.

## Optional components

To know how to test with or without optional components like a proxy, a Red Hat-like minion or a SSH minion, look at
the [optional components instructions](documentation/optional.md).

## Contributing

### Procedure

1. **Always** create a PR (even for backporting)
2. Your PR always needs at least one reviewer to approve

### Guidelines for coding

To get started, see the documentation about [Using and writing Cucumber steps](documentation/cucumber-steps.md). It
covers most common steps in an ordered manner, as well as the way to write new steps.

Please also read the [guidelines](documentation/guidelines.md) with attention. They cover style issues, idempotency
concerns, and naming conventions of files, features, scenarios and tests.

There are also hints about [Pitfalls](documentation/pitfalls.md) when writing code for the test suite.

## Branches used

- Development (to be run against a Main version of SUSE Manager):
  - [`master`](https://github.com/uyuni-project/uyuni)
- Release (to be run against a nightly or released *tagged* version of SUSE Manager):
  - [`Manager-5.1`](https://github.com/SUSE/spacewalk/tree/Manager-5.1)
  - [`Manager-5.0`](https://github.com/SUSE/spacewalk/tree/Manager-5.0)
  - [`Manager-4.3`](https://github.com/SUSE/spacewalk/tree/Manager-4.3)

## Dummy packages used by the test suite

Some of the scenarios that are tested in this test suite make use of some external testing repositories which contain
dummy packages. These packages are used to test package and patch installation and upgrade.

The Open Build System (OBS) projects are:

- [https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Pool](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Pool):
packages which must be installed on the client systems already;
- [https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Updates](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Test-Packages:Updates):
packages for `Test-Base-Channel-x86_64` and `Fake-Base-Channel-Debian-like` channels.

## Type of provided packages

- Normal dummy packages: `andromeda-dummy`, `hoag-dummy`, `orion-dummy`, `milkyway-dummy`, etc.
- Wrong encoding of RPM attributes: `blackhole-dummy`. This package should be successfully imported and you will see it
available as part of the `Test-Base-Channel-x86_64` if reposync handled the encoding correctly.
