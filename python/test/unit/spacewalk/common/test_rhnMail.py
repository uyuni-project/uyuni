#  pylint: disable=missing-module-docstring,invalid-name
import posix
import textwrap
from contextlib import contextmanager
from unittest.mock import MagicMock, call, patch

from spacewalk.common import rhnMail

dummy_uname = posix.uname_result(
    (
        "Linux",
        "myhostname",
        "5.16.5-1-default",
        "#1 SMP PREEMPT Sat Jan 1 00:00:00 UTC 2022 (badb007)",
        "x86_64",
    )
)

smtp_spy = MagicMock()


@contextmanager
def smtp_context_mock(domain):
    del domain  # we're not interested in the domain passed to SMTP()
    yield smtp_spy


@patch("spacewalk.common.rhnMail.os.uname", MagicMock(return_value=dummy_uname))
def test_send():
    passed_headers = None
    passed_body = "Test email"
    passed_sender = "test@example.com"
    sendmail = MagicMock()
    smtp_spy.sendmail = sendmail

    expected_body = textwrap.dedent(
        f"""\
    Subject: SUSE Manager System Mail From {dummy_uname.nodename}
    Content-Type: text/plain; charset=utf-8
    To: admin@example.com

    {passed_body}
    """
    )

    with patch("spacewalk.common.rhnMail.smtplib.SMTP", smtp_context_mock):
        rhnMail.send(passed_headers, passed_body, passed_sender)
        assert (
            call(passed_sender, ["admin@example.com"], expected_body)
            in sendmail.mock_calls
        )
