"""Unit tests for spacewalk.common.gpgverify"""

from spacewalk.common import gpgverify
import os
import pytest


def test_parse_signatures(gpg_verify_output):
    assert gpgverify.parse_signatures(gpg_verify_output) == [
        gpgverify.Signature(
            keyid="0E98404D386FA1D9",
            fingerprint="A7236886F3CCCAAD148A27F80E98404D386FA1D9",
            status="ERROR",
            username="",
        ),
        gpgverify.Signature(
            keyid="6ED0E7B82643E131",
            fingerprint="B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8",
            status="GOOD",
            username="Debian Archive Automatic Signing Key (12/bookworm) <ftpmaster@debian.org>",
        ),
        gpgverify.Signature(
            keyid="F8D2585B8783D481",
            fingerprint="4D64FEC119C2029067D6E791F8D2585B8783D481",
            status="GOOD",
            username="Debian Stable Release Key (12/bookworm) <debian-release@lists.debian.org>",
        ),
    ]


@pytest.mark.parametrize(
    "signature,expected",
    [
        (
            gpgverify.Signature(
                keyid="0E98404D386FA1D9",
                fingerprint="A7236886F3CCCAAD148A27F80E98404D386FA1D9",
                status="ERROR",
                username="",
            ),
            False,
        ),
        (
            gpgverify.Signature(
                keyid="F8D2585B8783D481",
                fingerprint="4D64FEC119C2029067D6E791F8D2585B8783D481",
                status="GOOD",
                username="Debian Stable Release Key (12/bookworm) <debian-release@lists.debian.org>",
            ),
            True,
        ),
    ],
)
def test_is_valid(signature, expected):
    assert gpgverify.is_valid(signature) == expected


class MockSubprocessCompletedProcess:
    def __init__(self, returncode=0, stdout=None, stderr=None):
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr


# pylint: disable-next=redefined-outer-name
@pytest.mark.parametrize("short_circuit,expected", [(True, True), (False, False)])
def test_verify_file_short_circuit(
    short_circuit, expected, gpg_verify_output, monkeypatch
):
    # pylint: disable-next=unused-argument
    def mock_run(*args, **kwargs):
        return MockSubprocessCompletedProcess(stdout=gpg_verify_output)

    monkeypatch.setattr(gpgverify, "_run", mock_run)
    monkeypatch.setattr(os, "access", lambda *a, **kw: True)

    assert (
        gpgverify.verify_file(signed_file="foo", short_circuit=short_circuit)[0]
        == expected
    )
