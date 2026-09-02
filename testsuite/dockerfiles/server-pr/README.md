# Thin image

This Dockerfile is a PR thin image. Sources are build by script `build_pr_changes.sh` which produce a single tarball
with all java, frontend, salt, python, schema, .. sources. Then new image is build based on original test image with
all sources copied over.

This is to mimic normal deployment as closely as possible including testing init scripts, radical schema changes and
similar.