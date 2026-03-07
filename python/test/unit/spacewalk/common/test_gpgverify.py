"""Unit tests for spacewalk.common.gpgverify"""

from spacewalk.common import gpgverify
import os
import textwrap
import pytest


# The fixture_ prefix in the function name is used to avoid the following pylint
# warning on tests that use the fixture
# W0621: Redefining name 'gpg_verify_output' from outer scope
@pytest.fixture(name="gpg_verify_output")
def fixture_gpg_verify_output():
    """
    Debian 12 (Bookworm) InRelease signed on 2025-08-09
    Keyring contains:
      - debian-archive-key-12-B7C5D7D6350947F8.asc
      - debian-archive-key-12-security-254CF3B5AEC0A8F0.asc
      - debian-release-12-F8D2585B8783D481.asc
    """

    yield textwrap.dedent("""
        [GNUPG:] NEWSIG
        [GNUPG:] ERRSIG 0E98404D386FA1D9 1 8 01 1754729632 9 A7236886F3CCCAAD148A27F80E98404D386FA1D9
        [GNUPG:] NO_PUBKEY 0E98404D386FA1D9
        [GNUPG:] NEWSIG
        [GNUPG:] KEY_CONSIDERED B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8 0
        [GNUPG:] SIG_ID JsSTrRiC5jaEhikim91zf/+2VjY 2025-08-09 1754729632
        [GNUPG:] GOODSIG 6ED0E7B82643E131 Debian Archive Automatic Signing Key (12/bookworm) <ftpmaster@debian.org>
        [GNUPG:] VALIDSIG 4CB50190207B4758A3F73A796ED0E7B82643E131 2025-08-09 1754729632 0 4 0 1 8 01 B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8
        [GNUPG:] KEY_CONSIDERED B8B80B5B623EAB6AD8775C45B7C5D7D6350947F8 0
        [GNUPG:] TRUST_UNDEFINED 0 pgp
        [GNUPG:] NEWSIG
        [GNUPG:] KEY_CONSIDERED 4D64FEC119C2029067D6E791F8D2585B8783D481 0
        [GNUPG:] SIG_ID 3Ya31wODEo6h+7bAfSp4t1wMMuw 2025-08-09 1754731533
        [GNUPG:] GOODSIG F8D2585B8783D481 Debian Stable Release Key (12/bookworm) <debian-release@lists.debian.org>
        [GNUPG:] VALIDSIG 4D64FEC119C2029067D6E791F8D2585B8783D481 2025-08-09 1754731533 0 4 0 22 8 01 4D64FEC119C2029067D6E791F8D2585B8783D481
        [GNUPG:] TRUST_UNDEFINED 0 pgp
        [GNUPG:] FAILURE gpg-exit 33554433
        """)


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
