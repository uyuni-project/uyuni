#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2016 Red Hat, Inc.
# Copyright (c) 2022 SUSE, LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
# this module implements the send mail support
#

import os
import smtplib

from rhn.connections import idn_puny_to_unicode
from spacewalk.common.rhnConfig import PRODUCT_NAME, cfg_component
from typing import NamedTuple, Union

with cfg_component(component=None) as CFG:
    PRODUCT_NAME = CFG.PRODUCT_NAME
    FALLBACK_TRACEBACK_MAIL = CFG.TRACEBACK_MAIL


class HeadersRecipients(NamedTuple):
    headers: dict
    to_addresses: Union[tuple, list]


def __check_headers(headers: dict = None) -> HeadersRecipients:
    """Ensure that the headers have the minimum required fields."""
    if not isinstance(headers, dict):
        headers = {}
    if "Subject" not in headers:
        hostname = idn_puny_to_unicode(os.uname().nodename)
        headers["Subject"] = f"{PRODUCT_NAME} System Mail From {hostname}"
    if "To" in headers:
        to = headers["To"]
    else:
        to = FALLBACK_TRACEBACK_MAIL
    if "Content-Type" not in headers:
        headers["Content-Type"] = "text/plain; charset=utf-8"
    if isinstance(to, (list, tuple)):
        toaddrs = to
        to = ", ".join(to)
    else:
        toaddrs = to.split(",")
    headers["To"] = to
    return HeadersRecipients(headers, toaddrs)


def send(headers: dict, body: str, sender=None):
    """Send an email with the passed content.

    The headers are checked for a minimum of fields, which will be filled with
    sensible defaults if they don't exist.

    :param headers: email headers
    :param body: email body
    :param sender: email "From" address, optional
    """
    headers_recipients = __check_headers(headers)
    if not headers_recipients:
        return

    if sender is None:
        sender = headers_recipients.headers["From"]
    joined_headers = "".join(
        [f"{k}: {v}\n" for k, v in headers_recipients.headers.items()]
    )
    msg = f"{joined_headers}\n{body}\n"

    with smtplib.SMTP("localhost") as smtp:
        smtp.sendmail(sender, headers_recipients.to_addresses, msg)
