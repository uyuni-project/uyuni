TESTS       = test_rhnLib.py test_usix_common.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	@PYTHONPATH=$(PYTHONPATH):../../.. $(PYTHON_BIN) $*
