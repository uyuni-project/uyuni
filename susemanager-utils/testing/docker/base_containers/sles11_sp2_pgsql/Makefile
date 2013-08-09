ifneq ($(strip $(shell docker images -q=true sles11_sp2_base)),)
SLE11_SP2_BASE_FOUND=1
endif

CONTAINER_NAME='sles11_sp2_pgsql'

build_container: check_base_system_available
	docker build -t $(CONTAINER_NAME) .

check_base_system_available:
ifndef SLE11_SP2_BASE_FOUND
$(error sles11_sp2_base container not found)
endif

all: build_container
