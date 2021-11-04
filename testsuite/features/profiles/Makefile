#
# generate profiles for specific environments from a template
#
TEMPLATE_DOCKER_DIR = Docker
TEMPLATE_KIWI_DIR   = Kiwi

REGISTRY_PLACEHOLDER_NOAUTH = registry.mgr.suse.de
REGISTRY_PLACEHOLDER_AUTH   = registry.mgr.suse.de:5000
ENVIRONMENTS                = internal_nue internal_prv
REG_REPLACEMENT_NUE_NOAUTH  = registry.mgr.suse.de
REG_REPLACEMENT_NUE_AUTH    = registry.mgr.suse.de:5000
REG_REPLACEMENT_PRV_NOAUTH  = registry.mgr.prv.suse.net
REG_REPLACEMENT_PRV_AUTH    = registry.mgr.prv.suse.net:5000

update: $(ENVIRONMENTS)

internal_nue: clean-internal_nue
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_DOCKER_DIR) $@/ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_AUTH)/$(REG_REPLACEMENT_NUE_AUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_NOAUTH)/$(REG_REPLACEMENT_NUE_NOAUTH)/g' ; \
	fi

internal_prv: clean-internal_prv
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_DOCKER_DIR) $@/ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_AUTH)/$(REG_REPLACEMENT_PRV_AUTH)/g' ; \
	  find $@ -type f | xargs sed -i 's/$(REGISTRY_PLACEHOLDER_NOAUTH)/$(REG_REPLACEMENT_PRV_NOAUTH)/g' ; \
	fi

clean-internal_nue:
	rm -rf internal_nue

clean-internal_prv:
	rm -rf internal_prv
