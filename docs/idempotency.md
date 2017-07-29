# Idempotency:

Prerequisites for each minion/Trad-client.

As a standard status, we require that tradclient and minions have the **base channel assigned**, where the test pkg are.

If you remove a tradclient or minion, **always readd** the base channel, otherwise all pkgs/patches test will fail.

For more prerequisites, look at core feature, but this is the only prerequisite for tradclient or minions.
