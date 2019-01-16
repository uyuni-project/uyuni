# Idempotency

We want features that that do not change their environment, so we get:
* idempotency: the faculty to run same the feature any number of times;
* commutativity: the ability to change the order in which features are run.

**Always** create an idempotent feature, and **always** as a secondary feature (a feature that is run after all core features). If the feature is not idempotent, it will be not merged.

We want idempotency at feature level. The individual scenarios are usually not idempotent.

To acheive idempotency, you may create preparation scenarios at the beginning of your scenarios to prepare for the other tests, and cleanup scenarios at the end of your features. The cleanup scenarios are prefixed with `Cleanup: `.

The rest of this document details individual requirements for idempotency.

## Registration

Each client must be correctly registered (in bootstrapped state) at the beginning of the feature.

## Base channel

As a standard status, we require that traditional clients and Salt minions have the **base channel assigned**. The base channel is where the test packages are.

If you remove a traditional client or minion, **always re-add** the base channel, otherwise all package and patch tests will fail.

## CentOS minion

CentOS clients are always registered as SSH salt minions by defaut.

## Patches tests

If you want to install packages and apply patches, you can have some trouble.
Since each features need to be idempotent, you need to:
 * not assume that some packages are present at beginning of your feature
 * cleanup at end of feature.

Basically, we use 2 repositories for test packages:

1) `Devel_Galaxy_BuildRepo`: contains lower version of packages, and is disabled by default;
2) `Test channel`: contains patches with higher version of packages, and is enabled. It is created by core features.

Typical workflow for patches test:

1) enable the BuildRepo
2) downgrade package (install) or remove package
3) schedule taskomatic run, and wait for it to finish (to retrieve patches available)
4) make test with patch (install it etc)
5) cleanup: remove packages and patches and disable the BuildRepo

Examples:

Take a look at this feature: ``features/trad_check_patches_install.feature``


## Build host

The Salt minion must always be ready to build containers and OS images.

Always re-enable these entitlements when you delete the minion and re-register it.


## Other

For more prerequisites, look at the core features.

