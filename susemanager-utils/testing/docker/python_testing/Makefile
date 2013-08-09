include ../common/common.mk

.PHONY: all
all: pgsql

vanilla:
	@echo Building vanilla container for python testing.
	$(call create_dockerfile, sles11_sp2_base)
	docker build -t manager_python_testing .
	$(call remove_dockerfile)

pgsql:
	@echo Building pythong testing contaner with PostgreSQL database.
	$(call create_dockerfile, sles11_sp2_pgsql)
	docker build -t manager_python_testing_pgsql .
	$(call remove_dockerfile)
