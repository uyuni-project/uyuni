#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2009--2016 Red Hat, Inc.
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

import os
import sys

# pylint: disable-next=deprecated-module
from optparse import Option, OptionParser

from spacewalk.common import rhnTB
from spacewalk.server import rhnSQL

from . import satCerts

DEFAULT_TRUSTED_CERT = "RHN-ORG-TRUSTED-SSL-CERT"


# pylint: disable-next=invalid-name
def processCommandline():
    options = [
        Option(
            "--ca-cert",
            action="store",
            default=DEFAULT_TRUSTED_CERT,
            type="string",
            # pylint: disable-next=consider-using-f-string
            help="public CA certificate, default is %s. If the value is '-' the CA is read from STDIN"
            % DEFAULT_TRUSTED_CERT,
        ),
        Option(
            "--label",
            action="store",
            default="RHN-ORG-TRUSTED-SSL-CERT",
            type="string",
            help="FOR TESTING ONLY - alternative database label for this CA certificate, "
            + 'default is "RHN-ORG-TRUSTED-SSL-CERT"',
        ),
        Option(
            "-v",
            "--verbose",
            action="count",
            help='be verbose (accumulable: -vvv means "be *really* verbose").',
        ),
    ]

    values, args = OptionParser(option_list=options).parse_args()

    # we take no extra commandline arguments that are not linked to an option
    if args:
        msg = (
            # pylint: disable-next=consider-using-f-string
            "ERROR: these arguments make no sense in this context (try "
            "--help): %s\n" % repr(args)
        )
        raise ValueError(msg)

    if values.ca_cert == "-":
        values.ca_cert = sys.stdin.read().strip()
    elif not os.path.exists(values.ca_cert):
        sys.stderr.write(
            # pylint: disable-next=consider-using-f-string
            "ERROR: can't find CA certificate at this location: "
            "%s\n" % values.ca_cert
        )
        sys.exit(10)

    # pylint: disable=W0703
    try:
        rhnSQL.initDB()
    except Exception:
        sys.stderr.write(
            # pylint: disable-next=consider-using-f-string
            """\
ERROR: there was a problem trying to initialize the database:

%s\n"""
            % rhnTB.fetchTraceback()
        )
        sys.exit(11)

    if values.verbose:
        # pylint: disable-next=consider-using-f-string
        print(("Public CA SSL certificate:  %s" % values.ca_cert))

    return values


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def main():
    """main routine
    10  CA certificate not found
    11  DB initialization failure
    13  Couldn't insert the certificate for whatever reason.
    """

    values = processCommandline()

    # pylint: disable-next=invalid-name
    def writeError(e):
        # pylint: disable-next=consider-using-f-string
        sys.stderr.write("\nERROR: %s\n" % e)

    try:
        satCerts.store_CaCert(values.label, values.ca_cert, verbosity=values.verbose)
    except satCerts.CaCertInsertionError:
        writeError(
            # pylint: disable-next=consider-using-f-string
            "Cannot insert certificate into DB!\n\n%s\n"
            % rhnTB.fetchTraceback()
        )
        sys.exit(13)
    return 0


# -------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.stderr.write(
        "\nWARNING: intended to be wrapped by another executable\n"
        "           calling program.\n"
    )
    sys.exit(main() or 0)
# ===============================================================================
