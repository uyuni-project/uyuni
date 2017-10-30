### HOWTO Patches/PKGs tests

###### [IMPROVE_ME]

If you want to make pkgs install and patches, you can have some troubles, since each features need to be idempotent:
you need to cleanup, and do not assume that some pkgs are present at beginn of your feature, and assume that you remove the pkg

Basically,  we use 2 REPOS for test packages:

1) contains lower version of pkgs:  Devel_Galaxy_BuildRepo  **disabled by default** 
2) contains patches with higher version of pkgs: Test channel **enabled, and created by core-feature.**

### typical WORKFLOW for patches test

0) enable the BuildRepo
1) downgrade pkg(install) /Remove pkg
3) schedule taskomatic run, and wait for finish. ( to retrieve patches avaibles)
4) make test with patch (install it etc)
5) cleanup: remove pkg/patches and disable the buildRepo

### Examples:

Take a look on this feature: features/trad_check_patches_install.feature``
