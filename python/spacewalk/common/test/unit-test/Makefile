TESTS       = \
        test_gettext.py \
        test_rhnCache.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	@PYTHONPATH=$(PYTHONPATH):../../.. $(PYTHON_BIN) $*
