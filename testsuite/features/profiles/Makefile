#
# generate profiles for specific environments from a template
#
TEMPLATE_DOCKER_DIR = Docker
TEMPLATE_KIWI_DIR   = Kiwi

DEFAULT_REPO = download.suse.de/ibs
REPO_NUE = minima-mirror-ci-bv.mgr.suse.de
REPO_SLC = minima-mirror-ci-bv.mgr.slc1.suse.org
REPO_AWS = ip-172-16-1-175.eu-central-1.compute.internal


REGISTRY_PLACEHOLDER_NOAUTH = registry.mgr.suse.de
REGISTRY_PLACEHOLDER_AUTH   = registry.mgr.suse.de:5000
ENVIRONMENTS                = internal_nue internal_slc cloud_aws
REG_REPLACEMENT_NUE_NOAUTH  = registry.mgr.suse.de
REG_REPLACEMENT_NUE_AUTH    = registry.mgr.suse.de:5000
REG_REPLACEMENT_SLC_NOAUTH  = registry.mgr.slc1.suse.org
REG_REPLACEMENT_SLC_AUTH    = registry.mgr.slc1.suse.org:5000
REG_REPLACEMENT_AWS_NOAUTH  = ip-172-16-1-175.eu-central-1.compute.internal
REG_REPLACEMENT_AWS_AUTH    = ip-172-16-1-175.eu-central-1.compute.internal:5000


update: $(ENVIRONMENTS)

internal_nue: clean-internal_nue
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_DOCKER_DIR) $@/ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_AUTH)/$(REG_REPLACEMENT_NUE_AUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_NOAUTH)/$(REG_REPLACEMENT_NUE_NOAUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)/SUSE/|$(REPO_NUE)/SUSE/|g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)|$(REPO_NUE)/ibs|g' ; \
	fi

internal_slc: clean-internal_slc
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_DOCKER_DIR) $@/ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_AUTH)/$(REG_REPLACEMENT_SLC_AUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_NOAUTH)/$(REG_REPLACEMENT_SLC_NOAUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)/SUSE/|$(REPO_SLC)/SUSE/|g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)|$(REPO_SLC)/ibs|g' ; \
	fi

cloud_aws: clean-cloud_aws
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_DOCKER_DIR) $@/ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_AUTH)/$(REG_REPLACEMENT_AWS_AUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_NOAUTH)/$(REG_REPLACEMENT_AWS_NOAUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)/SUSE/|$(REPO_AWS)/SUSE/|g' ; \
	  find $@ -type f | xargs sed -i 's|$(DEFAULT_REPO)|$(REPO_AWS)/ibs|g' ; \
	fi

clean-internal_nue:
	rm -rf internal_nue

clean-internal_slc:
	rm -rf internal_slc

clean-cloud_aws:
	rm -rf cloud_aws

