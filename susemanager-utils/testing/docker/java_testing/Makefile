include ../common/common.mk

.PHONY: all
all: pgsql

vanilla:
	@echo Building vanilla container for java testing.
	$(call create_dockerfile, sles11_sp2_base)
	docker build -t manager_java_testing .
	$(call remove_dockerfile)


pgsql:
	@echo Building java testing contaner with PostgreSQL database.
	$(call create_dockerfile, sles11_sp2_pgsql)
	docker build -t manager_java_testing_pgsql .
	$(call remove_dockerfile)

