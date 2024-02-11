#  pylint: disable=missing-module-docstring
from ..beacons import reboot_info
import pytest


def _reboot_not_required():
    return {"reboot_required": False}


def _reboot_required():
    return {"reboot_required": True}


context_reboot_required = {"reboot_needed": True}
context_reboot_not_required = {"reboot_needed": False}


@pytest.mark.parametrize(
    "context, module_fn, fire_event",
    [
        (
            # The __context__ is empty and reboot is not required, don't fire event.
            {},
            _reboot_not_required,
            False,
        ),
        (
            # The __context__ is empty and reboot is required, fire event.
            {},
            _reboot_required,
            True,
        ),
        (
            # The __context__ already register that reboot is required and it keeps, don't fire again.
            context_reboot_required,
            _reboot_required,
            False,
        ),
        (
            # The __context__ register that reboot is required and it changes, don't fire event.
            context_reboot_required,
            _reboot_not_required,
            False,
        ),
        (
            # The __context__ register that reboot isn't required but it changed, fire event.
            context_reboot_not_required,
            _reboot_required,
            True,
        ),
    ],
)
def test_beacon(context, module_fn, fire_event):
    reboot_info.__context__ = context
    reboot_info.__salt__ = {"reboot_info.reboot_required": module_fn}
    ret = reboot_info.beacon({})
    expected_result = [{"reboot_needed": True}] if fire_event else []
    assert ret == expected_result
