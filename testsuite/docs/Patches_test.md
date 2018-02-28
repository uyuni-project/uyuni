### How to test patches and packages

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
