"""Collection of context managers for Uyuni."""

# This module always had a dependency on spacewalk.common.rhnConfig, which is wrong.
# Everything in uyuni.common should work on clients as well. When we add other context
# managers (that are available on clients), this import should be guarded.
# Re-export to allow users to keep their import on uyuni.common.context_managers
# pylint: disable-next=unused-import
from spacewalk.common.rhnConfig import cfg_component
