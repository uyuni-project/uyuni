# Idempotency

## Registration

Each client must be correctly registered (in bootstrapped state) at the beginning of the feature.


## Base channel

As a standard status, we require that traditional clients and Salt minions have the **base channel assigned**. The base channel is where the test packages are.

If you remove a traditional client or minion, **always re-add** the base channel, otherwise all package and patch tests will fail.


## Container build host

The Salt minion must always be ready to build containers.

Always re-enable this entitlement when you remove it and re-register it.


## Other

For more prerequisites, look at the core features.
