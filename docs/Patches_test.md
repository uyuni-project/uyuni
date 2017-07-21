### HOWTO Patches/PKGs tests

###### [IMPROVE_ME]

If you want to make pkgs install and patches, you can have some troubles.

Since each feature must be idempotent, you need to cleanup.

Look at this.
features/trad_client_inst_package_and_patch.feature

Basically,  we use 2 REPOS:

FIXME: add links to repos

1) contains lower version of pkgs
2) contains patches with higher version

### WORKFLOW for patches

1) downgrade pkg /Remove pkg
2) Make the lowest pkg
3) schedule taskomatic run, and wait for finish.
4) make test with patch (install it etc)
5) cleanup

### Examples:

#### FIXME: this need adaptation (taskomatic isn't scheduled)

When you do patches test, you will always end-up with downgrading the pkg, install patch, remove it , and disable the repo.

```console
  Scenario: enable old-packages for test a patch install
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0-4.1" on "sle-client"

  Scenario: Install an patch to the trad-client
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 patch update has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed on "client"

  Scenario: Cleanup: remove virgo-dummy and restore non-update repo
    And I run "zypper -n rm andromeda-dummy" on "sle-client"
    And I run "zypper -n rm virgo-dummy" on "sle-client"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client"
```                                                                  
