#
# generate profiles for specific environments from a template
#
IMAGE_VERSION               = POS_Image_JeOS7
TEMPLATE_KIWI_DIR           = $(IMAGE_VERSION)
SERVICE_PACK                = SP4 SP5 SP6 SP7
SLES_FAMILY                 = SLES15
SLE_VERSION_PLACEHOLDER     = SLE_VERSION
SERVICE_PACK_PLACEHOLDER    = SERVICE_PACK

update: $(SERVICE_PACK)

SP4: clean-sp4
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $(IMAGE_VERSION)_$() $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK)/SLES15/g' ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/SP4/g' ; \
	fi

SP5: clean-sp5
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK)/SLES15/g' ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/SP5/g' ; \
	fi

SP6: clean-sp6
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK)/SLES15/g' ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/SP6/g' ; \
	fi

SP7: clean-sp7
	if test -n "$@" ; then \
	  mkdir $@ ; \
	  cp -r $(TEMPLATE_KIWI_DIR) $@/ ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK)/SLES15/g' ; \
	  find $@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/SP7/g' ; \
	fi

clean-sp4:
	rm -rf internal_nue

clean-sp5:
	rm -rf internal_prv

clean-sp6:
	rm -rf cloud_aws

clean-sp7:
	rm -rf cloud_aws
