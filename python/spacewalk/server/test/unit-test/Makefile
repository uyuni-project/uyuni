TESTS       = \
        test_rhnLib_timestamp.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	PYTHONPATH=$(PYTHONPATH):../../../ $(PYTHON_BIN) $*
