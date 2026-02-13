"""Verify the GPG signature of a file.

The GPG signature can exist as part of the signed document (`--sign` or `--clear-sign`),
or as a separate file (`--detached-sign`). This module runs `gpg --verify` and parses its
machine-readable output.

Public functions:
  - verify_file(): Returns a bool to indicate success, and a list of all signatures that
    were part of the verification. By default it short-circuits the verification when it
    finds the first signature that passes.
  - parse_signature(): Returns a list of signatures for a given GPG output.
  - is_valid(): Returns a bool to indicate whether a signature is considered valid.

See /usr/share/doc/packages/gpg2/DETAILS for gpg's machine-readable API documentation.
"""

import logging
import os
import subprocess
import typing
from datetime import datetime, timezone

logger = logging.getLogger(__name__)

# In Python 3.10+ we can annotate the alias "Path" with typing.TypeAlias
Path = typing.Union[str, os.PathLike]

SIG_STATUS = {
    "GOODSIG": "GOOD",
    "BADSIG": "BAD",
    "EXPSIG": "EXPIRED",
    "EXPKEYSIG": "KEY_EXPIRED",
    "REVKEYSIG": "KEY_REVOKED",
    "ERRSIG": "ERROR",
}

# Remove once we drop Python 3.6 support
if hasattr(typing, "Literal"):
    Status = typing.Literal[
        "GOOD", "BAD", "EXPIRED", "KEY_EXPIRED", "KEY_REVOKED", "ERROR"
    ]
else:
    Status = typing.Any


class Signature(typing.NamedTuple):
    keyid: str
    fingerprint: str
    status: Status
    username: str


def _run(
    *,
    signed_file: Path,
    signature_file: typing.Optional[Path],
    keyring: typing.Optional[Path],
):
    cmd = ["gpg", "--status-fd=1", "--verify"]
    if keyring:
        cmd.extend(("--keyring", str(keyring)))
    # Detached signature file comes first, if used
    if signature_file is not None:
        cmd.append(str(signature_file))
    cmd.append(str(signed_file))

    return subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
        encoding="utf-8",
        check=False,  # checked by caller
    )


class _LazyTimestampConverter:
    """Convert EPOCH to ISO-formatted time stamp when str() is called.

    This wrapper is used for lazy-logging EPOCH values as ISO-formatted time stamps.
    """

    def __init__(self, epoch):
        self.epoch = epoch

    def __str__(self):
        return datetime.fromtimestamp(int(self.epoch), tz=timezone.utc).isoformat()


def is_valid(signature: "Signature") -> bool:
    """Check if a signature is considered valid"""
    return signature.status == "GOOD"


def parse_signatures(gpg_output: str) -> typing.List["Signature"]:
    """Parse GPG output into a list Signature tuples.

    Args:
      gpg_output: gpg output in machine-readable format, i.e. executed with --status-fd.
    Returns: A list of Signature tuples.
    """
    signatures = []

    keyid = ""
    fpr = ""
    status = ""
    username = ""

    prefix = "[GNUPG:]"
    for line in gpg_output.splitlines():
        if not line.startswith(prefix):
            continue

        keyword, *args = line[len(prefix) :].strip().split(" ")

        if keyword == "NEWSIG":
            if keyid:  # Finish old signature
                signature = Signature(keyid, fpr, status, username)
                logger.debug("Parsed signature: %s", signature)
                signatures.append(signature)
            keyid = fpr = username = ""

        elif keyword == "KEY_CONSIDERED":
            fpr = args[0].strip()

        # Only one of GOODSIG, BADSIG, EXPSIG, EXPKEYSIG, REVKEYSIG or ERRSIG is expected
        # for each signature.
        elif keyword == "ERRSIG":
            # Avoid unpack errors if new arguments are added by GPG by limiting to
            # args[:7]
            keyid, _, _, _, timestamp, rc, fpr = args[:7]
            reason = ""
            if rc == "4":
                reason = "unknown hashing algorithm"
            elif rc == "9":
                reason = "unknown public key"
            logger.warning(
                "Signature verification failed. "
                "keyid='%s', fingerprint='%s', timestamp=%s, reason=%s.",
                keyid,
                fpr,
                _LazyTimestampConverter(timestamp),
                reason,
            )
            status = SIG_STATUS["ERRSIG"]

        # The rest in SIG_STATUS have the same structure
        elif keyword in SIG_STATUS:
            status = SIG_STATUS[keyword]
            keyid = args[0]
            username = " ".join(args[1:])

    if keyid:
        signature = Signature(keyid, fpr, status, username)
        logger.debug("Parsed signature: %s", signature)
        signatures.append(signature)
    return signatures


def verify_file(
    *,
    signed_file: Path,
    detached_signature_file: typing.Optional[Path] = None,
    keyring: typing.Optional[Path] = None,
    short_circuit: bool = True,
) -> typing.Tuple[bool, typing.List["Signature"]]:
    """Verify the GPG signature of a file.

    Args:
      signed_file: Filename of the file that is signed.
      detached_signature_file: Filename of a detached signature, optional.
        When this argument is not used, signed_file must include the signature.
      keyring: Path to a GPG public-keys keyring, often ends in pubring.pgp, optional.
      short_circuit: The verification short-circuits when it finds the first good signature.
        Default: True

    Returns: Tuple of (bool, list_of_signatures). The bool indicates if all / any
      (depending on short_circuit's value) signatures were verified successfully.

    Raises: OSError if passed paths are not accessible or the verification was terminated
      unexpectedly.
    """
    if not detached_signature_file:
        logger.debug(
            "No detached signature_file, assuming '%s' contains the signature.",
            signed_file,
        )

    for f in (signed_file, detached_signature_file, keyring):
        if f and not os.access(path=f, mode=os.R_OK):
            raise OSError(f"Can't access {f}.")

    completed = _run(
        signed_file=signed_file, signature_file=detached_signature_file, keyring=keyring
    )
    if completed.returncode < 0:
        raise OSError("Subprocess killed by signal.")

    signatures = parse_signatures(completed.stdout)
    if short_circuit:
        return any(is_valid(sig) for sig in signatures), signatures
    else:
        return all(is_valid(sig) for sig in signatures), signatures
