#  pylint: disable=missing-module-docstring
#
# DO NOT EDIT !!!
#

from spacewalk.common.rhnConfig import CFG

DOCUMENT_ROOT = CFG.documentroot

# package list format
#
# | alternative. Example: "a|b" when package "a" cannot be found try "b". First match wins.
#                One must be available.
# * optional. Example: "a*" if "a" is available add it, otherwise ignore it


PKGLIST12 = ["logrotate", "libsqlite3-0", "venv-salt-minion"]

RES7 = ["venv-salt-minion"]

RES7REQ = ["logrotate"]

RES7OPT = ["logrotate*"]

RES7_X86 = ["dmidecode*"]

RES8 = ["venv-salt-minion"]

RES8_X86 = ["dmidecode"]

RES9 = [
    "venv-salt-minion",
]

OPENEULER = [
    "venv-salt-minion",
]

PKGLIST15_SALT = [
    "venv-salt-minion",
]

PKGLISTMICRO_BUNDLE_ONLY = [
    "venv-salt-minion",
]

PKGLISTMICROOS_BUNDLE_ONLY = [
    "libnsl3",
    "libtirpc3",
    "libtirpc-netconfig",
    "venv-salt-minion",
]

PKGLISTTUMBLEWEED_BUNDLE_ONLY = [
    "venv-salt-minion",
]

PKGLIST15_X86_ARM = [
    "dmidecode",
    "libunwind",
]

PKGLIST15_PPC = [
    "libunwind",
]

PKGLIST15_Z = []

PKGLIST16_SALT = [
    "venv-salt-minion"
]

ONLYSLE16 = [
    "gio-branding-SLE-16",
]

PKGLIST16_X86_ARM = [
    "dmidecode",
    "libunwind8",
]

PKGLIST16_PPC = [
    "libunwind8",
]

PKGLIST16_Z = []

PKGLISTUBUNTU2004 = ["venv-salt-minion", "logrotate"]

PKGLISTUBUNTU2204 = ["venv-salt-minion", "logrotate"]

PKGLISTUBUNTU2404 = ["venv-salt-minion", "logrotate"]

PKGLISTDEBIAN11 = [
    # gnupg dependencies
    "dirmngr",
    "gnupg",
    "gnupg-l10n",
    "gnupg-utils",
    "gpg",
    "gpg-agent",
    "gpg-wks-client",
    "gpg-wks-server",
    "gpgconf",
    "gpgsm",
    "libassuan0",
    "libksba8",
    "libldap-2.4-2",
    "libldap-common",
    "libnpth0",
    "libsasl2-2",
    "libsasl2-modules",
    "libsasl2-modules-db",
    "libsqlite3-0",
    "pinentry-curses",
    # end of gnupg dependencies
    "venv-salt-minion",
]

PKGLISTDEBIAN12 = [
    # gnupg dependencies
    "dirmngr",
    "gnupg",
    "gnupg-l10n",
    "gnupg-utils",
    "gpg",
    "gpg-agent",
    "gpg-wks-client",
    "gpg-wks-server",
    "gpgconf",
    "gpgsm",
    "libassuan0",
    "libksba8",
    "libldap-2.5-0",
    "libnpth0",
    "libsasl2-2",
    "libsasl2-modules-db",
    "libsqlite3-0",
    "pinentry-curses",
    "readline-common",
    # end of gnupg dependencies
    "venv-salt-minion",
]

PKGLISTDEBIAN13 = [
    # gnupg dependencies
    "dirmngr",
    "gnupg",
    "gnupg-l10n",
    "gnupg-utils",
    "gpg",
    "gpg-agent",
    "gpg-wks-client",
    "gpgconf",
    "gpgsm",
    "gpgv",
    "libassuan9",
    "libffi8",
    "libgcrypt20",
    "libgnutls30t64",
    "libgpg-error-l10n",
    "libgpg-error0",
    "libgpm2",
    "libidn2-0",
    "libksba8",
    "libldap-common",
    "libldap2",
    "libncursesw6",
    "libnpth0t64",
    "libp11-kit0",
    "libreadline8t64",
    "libsasl2-2",
    "libsasl2-modules",
    "libsasl2-modules-db",
    "libtasn1-6",
    "libunistring5",
    "pinentry-curses",
    "readline-common",
    # end of gnupg dependencies
    "venv-salt-minion",
]

PKGLISTRASPBERRYPIOS12 = [
    # gnupg dependencies
    "dirmngr",
    "gnupg",
    "gnupg-l10n",
    "gnupg-utils",
    "gpg",
    "gpg-agent",
    "gpg-wks-client",
    "gpg-wks-server",
    "gpgconf",
    "gpgsm",
    "libassuan0",
    "libksba8",
    "libldap-2.5-0",
    "libnpth0",
    "libsasl2-2",
    "libsasl2-modules-db",
    "libsqlite3-0",
    "pinentry-curses",
    "readline-common",
    # end of gnupg dependencies
    "venv-salt-minion",
]

DATA = {
    "SLE-12-ppc64le": {
        "PDID": [1116, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-s390x": {
        "PDID": [1115, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-x86_64": {
        "PDID": [1117, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLES4SAP-12-x86_64": {
        "PDID": [1319, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-SP1-ppc64le": {
        "PDID": [1334, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLE-12-SP1-s390x": {
        "PDID": [1335, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLE-12-SP1-x86_64": {
        "PDID": [1322, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLES4SAP-12-SP1-ppc64le": {
        "PDID": [1437, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLES4SAP-12-SP1-x86_64": {
        "PDID": [1346, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "RES7-x86_64": {
        "PDID": [1251, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLL7-LTSS-x86_64": {
        "PDID": [2702, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLL7-OL-LTSS-x86_64": {
        "PDID": [2778, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7OPT,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLE-12-SP2-aarch64": {
        "PDID": [1375, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES_RPI-12-SP2-aarch64": {
        "PDID": [1418, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-ppc64le": {
        "PDID": [1355, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-s390x": {
        "PDID": [1356, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-x86_64": {
        "PDID": [1357, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES4SAP-12-SP2-x86_64": {
        "PDID": [1414, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES4SAP-12-SP2-ppc64le": {
        "PDID": [1521, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP3-aarch64": {
        "PDID": [1424, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-ppc64le": {
        "PDID": [1422, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-s390x": {
        "PDID": [1423, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-x86_64": {
        "PDID": [1421, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLED-12-SP3-x86_64": {
        "PDID": [1425, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": "/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLES4SAP-12-SP3-x86_64": {
        "PDID": [1426, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLES4SAP-12-SP3-ppc64le": {
        "PDID": [1572, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP4-aarch64": {
        "PDID": [1628, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-ppc64le": {
        "PDID": [1626, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-s390x": {
        "PDID": [1627, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-x86_64": {
        "PDID": [1625, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLED-12-SP4-x86_64": {
        "PDID": [1629, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLES4SAP-12-SP4-x86_64": {
        "PDID": [1755, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLES4SAP-12-SP4-ppc64le": {
        "PDID": [1754, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE4HPC-12-SP4-x86_64": {
        "PDID": [1759, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE4HPC-12-SP4-aarch64": {
        "PDID": [1758, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP5-aarch64": {
        "PDID": [1875, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-ppc64le": {
        "PDID": [1876, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-s390x": {
        "PDID": [1877, 3046],
        "BETAPDID": [3050],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-x86_64": {
        "PDID": [1878, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLES4SAP-12-SP5-x86_64": {
        "PDID": [1880, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLES4SAP-12-SP5-ppc64le": {
        "PDID": [1879, 3045],
        "BETAPDID": [3049],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE4HPC-12-SP5-x86_64": {
        "PDID": [1873, 3047],
        "BETAPDID": [3051],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE4HPC-12-SP5-aarch64": {
        "PDID": [1872, 3044],
        "BETAPDID": [3048],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "OES2018-SP3-x86_64": {
        "PDID": -21,
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "OES2023": {
        "PDID": -34,
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "OES23.4": {
        "PDID": -42,
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "OES24.4": {
        "PDID": -45,
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-aarch64": {
        "PDID": [1589, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-ppc64le": {
        "PDID": [1588, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-s390x": {
        "PDID": [1587, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-x86_64": {
        "PDID": [1576, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLES4SAP-15-ppc64le": {
        "PDID": [1588, 1613, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLES4SAP-15-x86_64": {
        "PDID": [1576, 1612, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-SP1-aarch64": {
        "PDID": [1769, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-ppc64le": {
        "PDID": [1770, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-s390x": {
        "PDID": [1771, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-x86_64": {
        "PDID": [1772, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLES4SAP-15-SP1-x86_64": {
        "PDID": [1772, 3055, 1766],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLES4SAP-15-SP1-ppc64le": {
        "PDID": [1770, 3053, 1765],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP2-aarch64": {
        "PDID": [1943, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-ppc64le": {
        "PDID": [1944, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-s390x": {
        "PDID": [1945, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-x86_64": {
        "PDID": [1946, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLES4SAP-15-SP2-x86_64": {
        "PDID": [1946, 3055, 1941],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLES4SAP-15-SP2-ppc64le": {
        "PDID": [1944, 3053, 1940],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP3-aarch64": {
        "PDID": [2142, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-ppc64le": {
        "PDID": [2143, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-s390x": {
        "PDID": [2144, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-x86_64": {
        "PDID": [2145, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLES4SAP-15-SP3-x86_64": {
        "PDID": [2145, 3055, 2136],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLES4SAP-15-SP3-ppc64le": {
        "PDID": [2143, 3053, 2135],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP4-aarch64": {
        "PDID": [2296, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-ppc64le": {
        "PDID": [2297, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-s390x": {
        "PDID": [2298, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-x86_64": {
        "PDID": [2299, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP5-aarch64": {
        "PDID": [2471, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-ppc64le": {
        "PDID": [2472, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-s390x": {
        "PDID": [2473, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-x86_64": {
        "PDID": [2474, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP6-aarch64": {
        "PDID": [2615, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-ppc64le": {
        "PDID": [2616, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-s390x": {
        "PDID": [2617, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-x86_64": {
        "PDID": [2618, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP7-aarch64": {
        "PDID": [2797, 3052],
        "BETAPDID": [3056],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-ppc64le": {
        "PDID": [2798, 3053],
        "BETAPDID": [3057],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-s390x": {
        "PDID": [2799, 3054],
        "BETAPDID": [3058],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-x86_64": {
        "PDID": [2800, 3055],
        "BETAPDID": [3059],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    # When adding new SLE15 Service packs, keep in mind the first PDID is for the BaseSystem product (not the base product)!
    "SLE-16.0-x86_64": {
        "PDID": [2930, 3246],
        "BETAPDID": [3250],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SLE-16.0-aarch64": {
        "PDID": [2931, 3247],
        "BETAPDID": [3251],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SLE-16.0-ppc64le": {
        "PDID": [2933, 3249],
        "BETAPDID": [3253],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SLE-16.0-s390x": {
        "PDID": [2932, 3248],
        "BETAPDID": [3252],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SLES4SAP-16.0-ppc64le": {
        "PDID": [2986, 3249],
        "BETAPDID": [3253],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SLES4SAP-16.0-x86_64": {
        "PDID": [2985, 3246],
        "BETAPDID": [3251],
        "PKGLIST": ONLYSLE16 + PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/16/0/bootstrap/",
    },
    "SUMA-43-PROXY-x86_64": {
        "PDID": [2299, 2384, 2379],
        "BETAPDID": [],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SUMA-43-SERVER-ppc64le": {
        "PDID": [2297, 2381, 2376],
        "BETAPDID": [],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SUMA-43-SERVER-s390x": {
        "PDID": [2298, 2382, 2377],
        "BETAPDID": [],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SUMA-43-SERVER-x86_64": {
        "PDID": [2299, 2383, 2378],
        "BETAPDID": [],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-MICRO-5.1-aarch64": {
        "PDID": [2282, 3064],
        "BETAPDID": [3067],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.1-s390x": {
        "PDID": [2287, 3065],
        "BETAPDID": [3068],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.1-x86_64": {
        "PDID": [2283, 3066],
        "BETAPDID": [3069],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.2-aarch64": {
        "PDID": [2399, 3064],
        "BETAPDID": [3067],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.2-s390x": {
        "PDID": [2400, 3065],
        "BETAPDID": [3068],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.2-x86_64": {
        "PDID": [2401, 3066],
        "BETAPDID": [3069],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.3-aarch64": {
        "PDID": [2426, 3064],
        "BETAPDID": [3067],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.3-s390x": {
        "PDID": [2427, 3065],
        "BETAPDID": [3068],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.3-x86_64": {
        "PDID": [2428, 3066],
        "BETAPDID": [3069],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.4-aarch64": {
        "PDID": [2572, 3064],
        "BETAPDID": [3067],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.4-s390x": {
        "PDID": [2573, 3065],
        "BETAPDID": [3068],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.4-x86_64": {
        "PDID": [2574, 3066],
        "BETAPDID": [3069],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.5-aarch64": {
        "PDID": [2603, 3064],
        "BETAPDID": [3067],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "SLE-MICRO-5.5-s390x": {
        "PDID": [2604, 3065],
        "BETAPDID": [3068],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "SLE-MICRO-5.5-x86_64": {
        "PDID": [2605, 3066],
        "BETAPDID": [3069],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "openSUSE-Leap-15.4-x86_64": {
        "PDID": [2409, 3055],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/4/bootstrap/",
    },
    "openSUSE-Leap-15.4-aarch64": {
        "PDID": [2406, 3052],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/4/bootstrap/",
    },
    "openSUSE-Leap-15.4-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_4-x86_64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/4/bootstrap/",
    },
    "openSUSE-Leap-15.4-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_4-aarch64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/4/bootstrap/",
    },
    "openSUSE-Leap-15.5-x86_64": {
        "PDID": [2588, 3055],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/5/bootstrap/",
    },
    "openSUSE-Leap-15.5-aarch64": {
        "PDID": [2585, 3052],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/5/bootstrap/",
    },
    "openSUSE-Leap-15.5-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_5-x86_64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/5/bootstrap/",
    },
    "openSUSE-Leap-15.5-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_5-aarch64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/5/bootstrap/",
    },
    "openSUSE-Leap-15.6-x86_64": {
        "PDID": [2734, 3055],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/6/bootstrap/",
    },
    "openSUSE-Leap-15.6-aarch64": {
        "PDID": [2731, 3052],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/6/bootstrap/",
    },
    "openSUSE-Leap-15.6-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_6-x86_64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/6/bootstrap/",
    },
    "openSUSE-Leap-15.6-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_leap15_6-aarch64",
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/6/bootstrap/",
    },
    "openSUSE-Leap-16.0-x86_64": {
        "PDID": [2921, 3246],
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_leap16_0-x86_64",
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-aarch64": {
        "PDID": [2922, 3247],
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_leap16_0-aarch64",
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-s390x": {
        "PDID": [2923, 3248],
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-s390x-uyuni": {
        "BASECHANNEL": "opensuse_leap16_0-s390x",
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-ppc64le": {
        "PDID": [2924, 3249],
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-16.0-ppc64le-uyuni": {
        "BASECHANNEL": "opensuse_leap16_0-ppc64le",
        "PKGLIST": PKGLIST16_SALT + PKGLIST16_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/16/0/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.3-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_3-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/3/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.3-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_3-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/3/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.4-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_4-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/4/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.4-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_4-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/4/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.5-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_5-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/5/bootstrap/",
    },
    "openSUSE-Leap-Micro-5.5-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro5_5-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/5/5/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.0-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_0-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/0/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.0-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_0-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/0/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.1-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_1-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/1/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.1-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_1-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/1/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.2-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_2-x86_64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/2/bootstrap/",
    },
    "openSUSE-Leap-Micro-6.2-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_micro6_2-aarch64",
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicro/6/2/bootstrap/",
    },
    "openSUSE-MicroOS-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_microos-x86_64",
        "PKGLIST": PKGLISTMICROOS_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicroos/latest/0/bootstrap/",
    },
    "openSUSE-MicroOS-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_microos-aarch64",
        "PKGLIST": PKGLISTMICROOS_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicroos/latest/0/bootstrap/",
    },
    "openSUSE-MicroOS-ppc64le-uyuni": {
        "BASECHANNEL": "opensuse_microos-ppc64le",
        "PKGLIST": PKGLISTMICROOS_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensusemicroos/latest/0/bootstrap/",
    },
    "openSUSE-Tumbleweed-x86_64-uyuni": {
        "BASECHANNEL": "opensuse_tumbleweed-x86_64",
        "PKGLIST": PKGLISTTUMBLEWEED_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT
        + "/pub/repositories/opensusetumbleweed/latest/0/bootstrap/",
    },
    "openSUSE-Tumbleweed-aarch64-uyuni": {
        "BASECHANNEL": "opensuse_tumbleweed-aarch64",
        "PKGLIST": PKGLISTTUMBLEWEED_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT
        + "/pub/repositories/opensusetumbleweed/latest/0/bootstrap/",
    },
    "openSUSE-Tumbleweed-ppc64le-uyuni": {
        "BASECHANNEL": "opensuse_tumbleweed-ppc64le",
        "PKGLIST": PKGLISTTUMBLEWEED_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT
        + "/pub/repositories/opensusetumbleweed/latest/0/bootstrap/",
    },
    "openSUSE-Tumbleweed-s390x-uyuni": {
        "BASECHANNEL": "opensuse_tumbleweed-s390x",
        "PKGLIST": PKGLISTTUMBLEWEED_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT
        + "/pub/repositories/opensusetumbleweed/latest/0/bootstrap/",
    },
    "SL-MICRO-6.0-aarch64": {
        "PDID": [2697, 3112],
        "BETAPDID": [3116],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.0-s390x": {
        "PDID": [2698, 3113],
        "BETAPDID": [3117],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.0-x86_64": {
        "PDID": [2699, 3111],
        "BETAPDID": [3115],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.1-aarch64": {
        "PDID": [2775, 3112],
        "BETAPDID": [3116],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-ppc64le": {
        "PDID": [2777, 3114],
        "BETAPDID": [3118],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-s390x": {
        "PDID": [2776, 3113],
        "BETAPDID": [3117],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-x86_64": {
        "PDID": [2774, 3111],
        "BETAPDID": [3115],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.2-aarch64": {
        "PDID": [2914, 3112],
        "BETAPDID": [3116],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/2/bootstrap/",
    },
    "SL-MICRO-6.2-ppc64le": {
        "PDID": [2916, 3114],
        "BETAPDID": [3118],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/2/bootstrap/",
    },
    "SL-MICRO-6.2-s390x": {
        "PDID": [2915, 3113],
        "BETAPDID": [3117],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/2/bootstrap/",
    },
    "SL-MICRO-6.2-x86_64": {
        "PDID": [2913, 3111],
        "BETAPDID": [3115],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/2/bootstrap/",
    },
    "centos-7-x86_64": {
        "PDID": [-12, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-7-aarch64": {
        "PDID": [-31, 3032],
        "BETAPDID": [3034],
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-8-x86_64": {
        "PDID": [-13, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "centos-8-aarch64": {
        "PDID": [-30, 3036],
        "BETAPDID": [3038],
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "centos-7-x86_64-uyuni": {
        "BASECHANNEL": "centos7-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-7-ppc64le-uyuni": {
        "BASECHANNEL": "centos7-ppc64le",
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-7-aarch64-uyuni": {
        "BASECHANNEL": "centos7-aarch64",
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-8-x86_64-uyuni": {
        "BASECHANNEL": "centos8-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "centos-8-ppc64le-uyuni": {
        "BASECHANNEL": "centos8-ppc64le",
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "centos-8-aarch64-uyuni": {
        "BASECHANNEL": "centos8-aarch64",
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "oracle-7-x86_64": {
        "PDID": [-14, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-7-aarch64": {
        "PDID": [-28, 3032],
        "BETAPDID": [3034],
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-8-x86_64": {
        "PDID": [-17, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/8/bootstrap/",
    },
    "oracle-8-aarch64": {
        "PDID": [-29, 3036],
        "BETAPDID": [3038],
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/8/bootstrap/",
    },
    "oracle-7-x86_64-uyuni": {
        "BASECHANNEL": "oraclelinux7-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-7-aarch64-uyuni": {
        "BASECHANNEL": "oraclelinux7-aarch64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-8-x86_64-uyuni": {
        "BASECHANNEL": "oraclelinux8-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/8/bootstrap/",
    },
    "oracle-8-aarch64-uyuni": {
        "BASECHANNEL": "oraclelinux8-aarch64",
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/8/bootstrap/",
    },
    "oracle-9-x86_64": {
        "PDID": [-41, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/9/bootstrap/",
    },
    "oracle-9-aarch64": {
        "PDID": [-40, 3040],
        "BETAPDID": [3042],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/9/bootstrap/",
    },
    "oracle-9-x86_64-uyuni": {
        "BASECHANNEL": "oraclelinux9-x86_64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/9/bootstrap/",
    },
    "oracle-9-aarch64-uyuni": {
        "BASECHANNEL": "oraclelinux9-aarch64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/9/bootstrap/",
    },
    "amazonlinux-2-x86_64": {
        "PDID": [-22, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2/bootstrap/",
    },
    "amazonlinux-2-aarch64": {
        "PDID": [-28, 3032],
        "BETAPDID": [3034],
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2/bootstrap/",
    },
    "amazonlinux-2-x86_64-uyuni": {
        "BASECHANNEL": "amazonlinux2-core-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2/bootstrap/",
    },
    "amazonlinux-2-aarch64-uyuni": {
        "BASECHANNEL": "amazonlinux2-core-aarch64",
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2/bootstrap/",
    },
    "amazonlinux-2023-x86_64-uyuni": {
        "BASECHANNEL": "amazonlinux2023-x86_64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2023/bootstrap/",
    },
    "amazonlinux-2023-aarch64-uyuni": {
        "BASECHANNEL": "amazonlinux2023-aarch64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2023/bootstrap/",
    },
    "amazonlinux-2023-x86_64": {
        "PDID": [-46, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2023/bootstrap/",
    },
    "amazonlinux-2023-aarch64": {
        "PDID": [-47, 3040],
        "BETAPDID": [3042],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2023/bootstrap/",
    },
    "RHEL7-x86_64": {
        "PDID": [-7, 3033],
        "BETAPDID": [3035],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "RHEL7-x86_64-uyuni": {
        "BASECHANNEL": "rhel7-pool-uyuni-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLE-ES8-x86_64": {
        "PDID": [-8, 1921, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "RHEL8-x86_64": {
        "PDID": [-8, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "RHEL8-x86_64-uyuni": {
        "BASECHANNEL": "rhel8-pool-uyuni-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "SUSE-LibertyLinux9-x86_64": {
        "PDID": [-35, 2538, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/9/bootstrap/",
    },
    "RHEL9-x86_64": {
        "PDID": [-35, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/9/bootstrap/",
    },
    "RHEL9-x86_64-uyuni": {
        "BASECHANNEL": "rhel9-pool-uyuni-x86_64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/9/bootstrap/",
    },
    "alibaba-2-x86_64-uyuni": {
        "BASECHANNEL": "alibaba-2-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/alibaba/2/bootstrap/",
    },
    "alibaba-2-aarch64-uyuni": {
        "BASECHANNEL": "alibaba-2-aarch64",
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/alibaba/2/bootstrap/",
    },
    "almalinux-8-x86_64": {
        "PDID": [-23, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/8/bootstrap/",
    },
    "almalinux-8-aarch64": {
        "PDID": [-26, 3036],
        "BETAPDID": [3038],
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/8/bootstrap/",
    },
    "almalinux-8-x86_64-uyuni": {
        "BASECHANNEL": "almalinux8-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/8/bootstrap/",
    },
    "almalinux-8-aarch64-uyuni": {
        "BASECHANNEL": "almalinux8-aarch64",
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/8/bootstrap/",
    },
    "almalinux-9-x86_64": {
        "PDID": [-38, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-aarch64": {
        "PDID": [-39, 3040],
        "BETAPDID": [3042],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-x86_64-uyuni": {
        "BASECHANNEL": "almalinux9-x86_64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-aarch64-uyuni": {
        "BASECHANNEL": "almalinux9-aarch64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-ppc64le-uyuni": {
        "BASECHANNEL": "almalinux9-ppc64le",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-s390x-uyuni": {
        "BASECHANNEL": "almalinux9-s390x",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "rockylinux-8-x86_64": {
        "PDID": [-24, 3037],
        "BETAPDID": [3039],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/8/bootstrap/",
    },
    "rockylinux-8-aarch64": {
        "PDID": [-27, 3036],
        "BETAPDID": [3038],
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/8/bootstrap/",
    },
    "rockylinux-8-x86_64-uyuni": {
        "BASECHANNEL": "rockylinux8-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/8/bootstrap/",
    },
    "rockylinux-8-aarch64-uyuni": {
        "BASECHANNEL": "rockylinux8-aarch64",
        "PKGLIST": RES8,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/8/bootstrap/",
    },
    "rockylinux-9-x86_64": {
        "PDID": [-36, 3041],
        "BETAPDID": [3043],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-aarch64": {
        "PDID": [-37, 3040],
        "BETAPDID": [3042],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-x86_64-uyuni": {
        "BASECHANNEL": "rockylinux9-x86_64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-aarch64-uyuni": {
        "BASECHANNEL": "rockylinux9-aarch64",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-ppc64le-uyuni": {
        "BASECHANNEL": "rockylinux9-ppc64le",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-s390x-uyuni": {
        "BASECHANNEL": "rockylinux9-s390x",
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "ubuntu-22.04-amd64": {
        "PDID": [-33, 3060],
        "BETAPDID": [3061],
        "PKGLIST": PKGLISTUBUNTU2204,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/22/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-24.04-amd64": {
        "PDID": [-44, 3062],
        "BETAPDID": [3063],
        "PKGLIST": PKGLISTUBUNTU2404,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/24/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-22.04-amd64-uyuni": {
        "BASECHANNEL": "ubuntu-22.04-pool-amd64-uyuni",
        "PKGLIST": PKGLISTUBUNTU2204,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/22/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-24.04-amd64-uyuni": {
        "BASECHANNEL": "ubuntu-24.04-pool-amd64-uyuni",
        "PKGLIST": PKGLISTUBUNTU2404,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/24/4/bootstrap/",
        "TYPE": "deb",
    },
    "debian12-amd64": {
        "PDID": [-43, 3028],
        "BETAPDID": [3030],
        "PKGLIST": PKGLISTDEBIAN12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/12/bootstrap/",
        "TYPE": "deb",
    },
    "debian12-amd64-uyuni": {
        "BASECHANNEL": "debian-12-pool-amd64-uyuni",
        "PKGLIST": PKGLISTDEBIAN12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/12/bootstrap/",
        "TYPE": "deb",
    },
    "raspberrypios-12-arm64-uyuni": {
        "BASECHANNEL": "raspberrypios-12-pool-arm64-uyuni",
        "PKGLIST": PKGLISTRASPBERRYPIOS12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/12/bootstrap/",
        "TYPE": "deb",
    },
    "raspberrypios-12-armhf-uyuni": {
        "BASECHANNEL": "raspberrypios-12-pool-armhf-uyuni",
        "PKGLIST": PKGLISTRASPBERRYPIOS12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/raspbian/12/bootstrap/",
        "TYPE": "deb",
    },
    "debian13-amd64": {
        # TODO: Use the right PDIDs
        #"PDID": [-43, 3028],
        #"BETAPDID": [3030],
        "PKGLIST": PKGLISTDEBIAN13,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/13/bootstrap/",
        "TYPE": "deb",
    },
    "debian13-amd64-uyuni": {
        "BASECHANNEL": "debian-13-pool-amd64-uyuni",
        "PKGLIST": PKGLISTDEBIAN13,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/13/bootstrap/",
        "TYPE": "deb",
    },
    "raspberrypios-13-arm64-uyuni": {
        "BASECHANNEL": "raspberrypios-13-pool-arm64-uyuni",
        "PKGLIST": PKGLISTDEBIAN13,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/13/bootstrap/",
        "TYPE": "deb",
    },
    "raspberrypios-13-armhf-uyuni": {
        "BASECHANNEL": "raspberrypios-13-pool-armhf-uyuni",
        "PKGLIST": PKGLISTDEBIAN13,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/raspbian/13/bootstrap/",
        "TYPE": "deb",
    },
    "openeuler22.03-x86_64-uyuni": {
        "BASECHANNEL": "openeuler2203-x86_64",
        "PKGLIST": OPENEULER,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/22.03/bootstrap/",
    },
    "openeuler22.03-aarch64-uyuni": {
        "BASECHANNEL": "openeuler2203-aarch64",
        "PKGLIST": OPENEULER,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/22.03/bootstrap/",
    },
    "openeuler24.03-SP1-x86_64": {
        "PDID": [-49, 3041],
        "BETAPDID": [3043],
        "PKGLIST": OPENEULER,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/24.03/bootstrap/",
    },
    "openeuler24.03-SP1-aarch64": {
        "PDID": [-50, 3040],
        "BETAPDID": [3042],
        "PKGLIST": OPENEULER,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/24.03/bootstrap/",
    },
    "raspberrypios12-arm64": {
        "PDID": [-48, 3029],
        "BETAPDID": [3031],
        "PKGLIST": PKGLISTRASPBERRYPIOS12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/raspbian/12/bootstrap/",
        "TYPE": "deb",
    },
}
