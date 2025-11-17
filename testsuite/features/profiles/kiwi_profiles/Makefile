#
# generate profiles for specific environments from a template
#
IMAGE_VERSION               = POS_Image-JeOS7
TEMPLATE_KIWI_DIR           = $(IMAGE_VERSION)
SERVICE_PACK                = SP4 SP5 SP6 SP7
SLES_FAMILY                 = SLES15
SLE_VERSION_PLACEHOLDER     = SLE_VERSION
SERVICE_PACK_PLACEHOLDER    = SERVICE_PACK

update: $(SERVICE_PACK)

# --- Main Generation Rule ---
# Static pattern rule to generate all Service Pack profiles (SP4, SP5, SP6, SP7).
# The '%' represents the stem (e.g., 'SP4') which is the target name ($@).
$(SERVICE_PACK): %: clean-%
	@echo "-> Generating Kiwi profile for SLES15-$@"
	# Copy template to the new profile directory: SPX/POS_Image_JeOS7_SLES15SPX
	@cp -r $(TEMPLATE_KIWI_DIR) $(IMAGE_VERSION)_$(SLES_FAMILY)$@ ;
	# Replace generic SLE_VERSION placeholder with SLES15
	@find $(IMAGE_VERSION)_$(SLES_FAMILY)$@ -type f | xargs sed -i 's/$(SLE_VERSION_PLACEHOLDER)/$(SLES_FAMILY)/g' ;
	# Replace SERVICE_PACK placeholder with the current service pack (e.g., SP4)
	@find $(IMAGE_VERSION)_$(SLES_FAMILY)$@ -type f | xargs sed -i 's/$(SERVICE_PACK_PLACEHOLDER)/$@/g' ;

# --- Cleanup Rules ---

# Generic pattern rule to define prerequisites and clean up the generated SPX directories.
# The '$*' variable is the stem (e.g., 'SP4').
clean-%:
	@rm -rf $*

.PHONY: $(SERVICE_PACK) clean update