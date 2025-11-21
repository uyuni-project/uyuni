#
# generate profiles for specific environments from a template
#
IMAGE_VERSION               = POS_Image-JeOS7
TEMPLATE_KIWI_DIR           = $(IMAGE_VERSION)
SERVICE_PACK                = SP4 SP5 SP6 SP7
SLES_FAMILY                 = SLES15
SLE_VERSION_PLACEHOLDER     = SLE_VERSION
SERVICE_PACK_PLACEHOLDER    = SERVICE_PACK

update: clean-all $(SERVICE_PACK)

# --- Main Generation Rule ---
$(SERVICE_PACK): %:
	@echo "-> Generating Kiwi profile for SLES15-$@"
	@cp -r $(TEMPLATE_KIWI_DIR) $(IMAGE_VERSION)_$(SLES_FAMILY)$@ ;
	@find $(IMAGE_VERSION)_$(SLES_FAMILY)$@ -type f | xargs sed -i 's/$(SLE_VERSION_PLACEHOLDER)/$(SLES_FAMILY)/g' ;
	@find $(IMAGE_VERSION)_$(SLES_FAMILY)$@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/$@/g' ;

clean-all:
	@echo "-> Cleaning all generated Kiwi profiles: $(IMAGE_VERSION)_$(SLES_FAMILY)SP*"
	@rm -rf $(IMAGE_VERSION)_$(SLES_FAMILY)SP*

.PHONY: $(SERVICE_PACK) update
