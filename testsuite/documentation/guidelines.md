# Guidelines for developing the test suite

## Directory layout

* `testsuite/config`: contains the Cucumber profiles
* `testsuite/documentation`: the test suite documentation
* `testsuite/features`: definition of tests of software features
* `testsuite/features/profiles`: Docker and Kiwi profiles picked up from this git repository directly by Uyuni
* `testsuite/features/step_definition`: definition of real code executed, written in Ruby
* `testsuite/features/support`: general support functions
* `testsuite/features/upload_files`: various data files uploaded into test machines by the test suite
* `testsuite/run_sets`: YAML files containing all features that get executed

## Cucumber profiles

We have a list of Cucumber profiles that filter all Cucumber features by a tag with the same name that fits in this
focus area, e.g. all Cobbler related features are tagged with `@scope_cobbler`.

You can find all profiles in [testsuite/config/cucumber.yml](https://github.com/uyuni-project/uyuni/blob/master/testsuite/config/cucumber.yml)

If you run the tests using the Rake tasks, you can pass a list of profiles using an environment variable.
Example:

```bash
export TAGS=@scope_cobbler

# run secondary tests
rake cucumber:secondary

# remove the env. variable afterwards
unset TAGS
```

## Grouping steps

By convention in this test suite,
"Steps should be grouped by topic and not by feature.".

This sentence means
that the steps are grouped by areas of the product (like "salt") or things
to test (like "content steps"), and do not relate to the organization of
the cucumber "feature" files.

## Reusing steps

Another rule is to reuse existing steps instead of creating
new ones. But be careful, it is tempting to re-create a programming language
with lots of variables, instead of creating steps that have a natural name
in English.

A good starting point to see which steps are already implemented is
the documentation about [using and writing Cucumber steps](cucumber-steps.md).
This documentation describes the most common steps, for an
exhaustive list, look at the steps themselves under `features/step_definitions/`.

## Idempotency

Idempotency is the faculty to run same the feature any number of times. The basic idea of such a feature
is that it does not change its environment. Our secondary features (the features run after the core features)
are supposed to be idempotent.

Idempotency is a topic of its own. Please refer to [idempotency documentation](idempotency.md) for details.

## Phrasing

* Keep features, scenarios and steps in fluent English.
* Avoid "computing style" (abbreviations, snake case and camel case).
* Avoid the obvious like "Check", "Test", "Uyuni", "Spacewalk", etc.
* Please use the correct capitalization for products (SLES, SUSE, Salt, zypper, Docker, etc.)
* Avoid reinventing function names and variables. Cucumber is all about human-readable text, factorizing or other
things that make a computer program efficient are second class citizens with Cucumber.

## Rules for features

* Features are grouped by the stage in which they are executed, using the folders `core`, `reposync`, `secondary`,
`init_clients` and `finishing`
* Use the right prefix for the filename of the file that will contain your feature, following this format:
`<type>_<topic>`
* `<type>` is currently one of:
  * "srv": feature testing server side
  * "proxy": feature testing proxy side
  * "min": feature testing SLES minions (not SSH)
  * "minssh": feature testing SSH SLES minions
  * "buildhost": feature using Kiwi and Docker build host
  * "minkvm": feature testing KVM host SLES minions
  * "rhlike": feature testing Red Hat-like minions (CentOS, Alma, Rocky, ...)
  * "deblike": feature testing Debian-like minions (Debian, Ubuntu, ...)
  * "allcli: feature testing all clients
* `<topic>` must contain "salt" or "docker" for features related to Salt or Docker, and is then specific to the feature.
* Inside `init_clients` features we'll see the features in charge of the bootstrap process for each client. They will
follow the format:
  * Continuous Integration test suite (`features/init_clients`): `<distribution>`_`<client|minion|ssh_minion>`.
  Example: `sle_ssh_minion.feature`
  * Build Validation test suite (`features/build_validation/init_clients`): `<distribution><version>`_`<minion|ssh_minion>`.
  Example: `sle15sp4_ssh_minion.feature`
* File name examples: `srv_reboot_server.feature`, `srv_salt_ping.feature`, `srv_salt_service.feature`,
`min_salt_bootstrap.feature`, or `min_ping.feature`

Inside those files:
* Feature names start with upper case
* Don't use duplicate feature names
* Use standard "In order to ... As ... I want to ..." for the feature description
* Feature name and description are about a feature *of the product*, not about some test to be done. Describe the
product's feature, not how you want to test it. This is the role of the scenarios.

## Rules for scenarios

* Scenario names start with upper case
* Scenario names are sentences based on verbs in imperative form
* Don't use duplicate scenarios names
* Scenario names describe the test
* A scenario to prepare before the actual tests should be named: ```Scenario: Pre-requisite: do something...```
* A scenario that cleans up should be named: ```Scenario: Cleanup: do something...```
* Do not detail specifics (user name, package name, etc.) in the scenario names
* Avoid parentheses in scenario names (painful from command line), use comments (```#```) instead.

## Rules for steps

* Steps start with lower case (unless first person "I", abbreviation, etc.)
* Don't use internals like ```#accept-btn```in steps, try to hide them with the scenario name
* Use meaningful step names
* Use verbs that match the role of the step:
  * "... is", "I am" or "... are" for "Given" steps (preconditions)
  * "I ..." for "When" steps (actions)
  * "... should" for "Then" steps (checks)
* Don't use "we", use "I"
* Put quotes (") around string parameters, not around numeric parameters
* Don't use steps parameters if unneeded
* Don't use too many steps parameters (use cucumber tables)
* Don't fall in the case where same step can trigger two different regular expressions

In the implementation:
* We are phasing out minitest assertions like ```assert_equal```, use `fail` instead
* Don't use global variables, prefer member variables
* Inside a step definition use `log` instead of `puts`, as the `puts` method will not be capture by Cucumber.

## Other

See [pitfalls](pitfalls.md) for more details on the problems you might face and for more recommendations.
