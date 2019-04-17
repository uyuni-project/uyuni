## Guidelines for writing the testsuite

### Directory layout

* `testsuite/features`: definition of tests of software features, in English
* `testsuite/features/step_definition`: definition of real code executed, in Ruby
* `testsuite/features/support`: general support functions
* `testsuite/features/upload_files`: various data files uploaded into test machines by the testsuite


### Grouping steps

By convention in this testsuite,
"Steps should be grouped by topic and not by feature.".

This sentence means
that the steps are grouped by areas of the product (like "salt") or things
to test (like "content steps"), and do not relate to the organization of
the cucumber "feature" files.


### Reusing steps

Another rule is to reuse existing steps instead of creating
new ones. But be careful, it is tempting to re-create a programming language
with lots of variables, instead of creating steps that have a natural name
in English.

A good starting point to see which steps are already implemented is
the documentation about [using and writing Cucumber steps](cucumber-steps.md).
This documentation describes the most common steps, for an
exhaustive list, look at the steps themselves under `features/step_definitions/`.


### Idempotency

Idempotency is the faculty to run same the feature any number of times. The basic idea of such a feature is that it does not change its environment. Our secondary features (the features run after the core features) are supposed to be idempotent.

Idempotency is a topic of its own. Please refer to [idempotency documentation](idempotency.md) for details.


### Phrasing

Keep features, scenarios and steps in fluent English.

Avoid "computing style" (abbreviations, snake case and camel case.

Avoid the obvious like "Check", "Test", "SUSE Manager", "Spacewalk", etc.

Please use the correct capitalization for products (SLES, SUSE, Salt, zypper, Docker, etc.)

Avoid reinventing function names and variables. Cucumber is all about human-readable text, factorizing or other things that make a computer program efficient are second class citizens with Cucumber.


### Rules for features

 * Use the right prefix for the filename of the file that will contain your feature.
 * Naming of core feature files: core_<type>_<topic>.
 * Naming of secondary feature files: <type>_<topic>.
 * <type> is currently one of:
   * "srv": feature testing server side
   * "min": feature testing SLES minions (not SSH)
   * "minssh": feature testing SSH SLES minions
   * "centos": feature testing CentOS (should become "rhes" in the future when we start using Extended Support images)
   * "trad": feature testing tradional client
   * "allcli: feature testing all clients
 * <topic> must contain "salt" or "docker" for features related to salt or docker, and is then specific to the feature.
 * File name examples: "srv_reboot_server.feature", "srv_salt_ping.feature", "srv_salt_service.feature", "min_salt_bootstrap.feature", ot "min_ping.feature"

 * Inside those files, feature names start with upper case
 * Don't use duplicate feature names
 * Use standard "In order to ... As ... I want to ..." for the feature description
 * Feature name and description are about a feature *of the product*, not about some test to be done. Describe the product's feature, not how you want to test it. This is the role of the scenarios.


### Rules for scenarios

 * Scenario names start with upper case
 * Scenario names are sentences based on verbs in imperative form
 * Don't use duplicate scenarios names (checked at commit time)
 * Scenario names describe the test
 * A scenario to prepare before the actual tests should be named: ```Scenario: pre-requisite: ...```
 * A scenario that cleans up should be named: ```Scenario: cleanup: ...```
 * Do not detail specifics (user name, package name, etc.) in the scenario names
 * Avoid parentheses in scenario names (painful from command line), use comments (#) instead


### Rules for steps

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


### Rules for Ruby implementation of steps

 * Don't use ```fail```, prefer minitest assertions like ```assert_equal```
 * Don't use global variables, prefer member variables

### Other

See [pitfalls documentation](pitfalls.md) for more details on the problems you might face and more recommandations.
