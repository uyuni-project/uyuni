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

OPENEULER2203 = [
    "venv-salt-minion",
]

PKGLIST15_SALT = [
    "python3-ply",
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

PKGLISTUBUNTU2004 = [
    "venv-salt-minion",
]

PKGLISTUBUNTU2204 = [
    "venv-salt-minion",
]

PKGLISTUBUNTU2404 = [
    "venv-salt-minion",
]

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
        "PDID": 1116,
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-s390x": {
        "PDID": 1115,
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-x86_64": {
        "PDID": 1117,
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLES4SAP-12-x86_64": {
        "PDID": 1319,
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/0/bootstrap/",
    },
    "SLE-12-SP1-ppc64le": {
        "PDID": 1334,
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLE-12-SP1-s390x": {
        "PDID": [1335, 1535],
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLE-12-SP1-x86_64": {
        "PDID": [1322, 1533],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLES4SAP-12-SP1-ppc64le": {
        "PDID": 1437,
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "SLES4SAP-12-SP1-x86_64": {
        "PDID": 1346,
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/1/bootstrap/",
    },
    "RES7-x86_64": {
        "PDID": [1251],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLL7-LTSS-x86_64": {
        "PDID": [2702, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLL7-OL-LTSS-x86_64": {
        "PDID": [2778, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7OPT,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLE-12-SP2-aarch64": {
        "PDID": 1375,
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES_RPI-12-SP2-aarch64": {
        "PDID": 1418,
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-ppc64le": {
        "PDID": [1355, 1737],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-s390x": {
        "PDID": [1356, 1738],
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP2-x86_64": {
        "PDID": [1357, 1739],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES4SAP-12-SP2-x86_64": {
        "PDID": 1414,
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLES4SAP-12-SP2-ppc64le": {
        "PDID": 1521,
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/2/bootstrap/",
    },
    "SLE-12-SP3-aarch64": {
        "PDID": [1424, 2002],
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-ppc64le": {
        "PDID": [1422, 1930],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-s390x": {
        "PDID": [1423, 1931],
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP3-x86_64": {
        "PDID": [1421, 1932],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLED-12-SP3-x86_64": {
        "PDID": [1425],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": "/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLES4SAP-12-SP3-x86_64": {
        "PDID": 1426,
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLES4SAP-12-SP3-ppc64le": {
        "PDID": 1572,
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/3/bootstrap/",
    },
    "SLE-12-SP4-aarch64": {
        "PDID": [1628, 2114],
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-ppc64le": {
        "PDID": [1626, 2115],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-s390x": {
        "PDID": [1627, 2116],
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP4-x86_64": {
        "PDID": [1625, 2117],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLED-12-SP4-x86_64": {
        "PDID": [1629],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLES4SAP-12-SP4-x86_64": {
        "PDID": [1755],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLES4SAP-12-SP4-ppc64le": {
        "PDID": [1754],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE4HPC-12-SP4-x86_64": {
        "PDID": [1759],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE4HPC-12-SP4-aarch64": {
        "PDID": [1758],
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/4/bootstrap/",
    },
    "SLE-12-SP5-aarch64": {
        "PDID": [1875],
        "BETAPDID": [1744],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-ppc64le": {
        "PDID": [1876],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-s390x": {
        "PDID": [1877],
        "BETAPDID": [1746],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE-12-SP5-x86_64": {
        "PDID": [1878],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLES4SAP-12-SP5-x86_64": {
        "PDID": [1880],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLES4SAP-12-SP5-ppc64le": {
        "PDID": [1879],
        "BETAPDID": [1745],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE4HPC-12-SP5-x86_64": {
        "PDID": [1873],
        "BETAPDID": [1747],
        "PKGLIST": PKGLIST12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/12/5/bootstrap/",
    },
    "SLE4HPC-12-SP5-aarch64": {
        "PDID": [1872],
        "BETAPDID": [1744],
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
    "SLE-15-aarch64": {
        "PDID": [1589, 2053, 1709],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-ppc64le": {
        "PDID": [1588, 2054, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-s390x": {
        "PDID": [1587, 2055, 1711],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-x86_64": {
        "PDID": [1576, 2056, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLES4SAP-15-ppc64le": {
        "PDID": [1588, 1613, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLES4SAP-15-x86_64": {
        "PDID": [1576, 1612, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/0/bootstrap/",
    },
    "SLE-15-SP1-aarch64": {
        "PDID": [1769, 1709, 2216],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-ppc64le": {
        "PDID": [1770, 1710, 2217],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-s390x": {
        "PDID": [1771, 1711, 2218],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP1-x86_64": {
        "PDID": [1772, 1712, 2219],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SUMA-40-PROXY-x86_64": {
        "PDID": [1772, 1908],
        "BETAPDID": [],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLES4SAP-15-SP1-x86_64": {
        "PDID": [1772, 1712, 1766],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLES4SAP-15-SP1-ppc64le": {
        "PDID": [1770, 1710, 1765],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/1/bootstrap/",
    },
    "SLE-15-SP2-aarch64": {
        "PDID": [1943, 1709, 2372],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-ppc64le": {
        "PDID": [1944, 1710, 2373],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-s390x": {
        "PDID": [1945, 1711, 2374],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP2-x86_64": {
        "PDID": [1946, 1712, 2375],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLES4SAP-15-SP2-x86_64": {
        "PDID": [1946, 1712, 1941],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLES4SAP-15-SP2-ppc64le": {
        "PDID": [1944, 1710, 1940],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/2/bootstrap/",
    },
    "SLE-15-SP3-aarch64": {
        "PDID": [2142, 1709, 2567],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-ppc64le": {
        "PDID": [2143, 1710, 2568],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-s390x": {
        "PDID": [2144, 1711, 2569],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP3-x86_64": {
        "PDID": [2145, 1712, 2570],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLES4SAP-15-SP3-x86_64": {
        "PDID": [2145, 1712, 2136],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLES4SAP-15-SP3-ppc64le": {
        "PDID": [2143, 1710, 2135],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/3/bootstrap/",
    },
    "SLE-15-SP4-aarch64": {
        "PDID": [2296, 1709],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-ppc64le": {
        "PDID": [2297, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-s390x": {
        "PDID": [2298, 1711],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP4-x86_64": {
        "PDID": [2299, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/4/bootstrap/",
    },
    "SLE-15-SP5-aarch64": {
        "PDID": [2471, 1709],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-ppc64le": {
        "PDID": [2472, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-s390x": {
        "PDID": [2473, 1711],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP5-x86_64": {
        "PDID": [2474, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/5/bootstrap/",
    },
    "SLE-15-SP6-aarch64": {
        "PDID": [2615, 1709],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-ppc64le": {
        "PDID": [2616, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-s390x": {
        "PDID": [2617, 1711],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP6-x86_64": {
        "PDID": [2618, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/6/bootstrap/",
    },
    "SLE-15-SP7-aarch64": {
        "PDID": [2797, 1709],
        "BETAPDID": [1925],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-ppc64le": {
        "PDID": [2798, 1710],
        "BETAPDID": [1926],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_PPC,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-s390x": {
        "PDID": [2799, 1711],
        "BETAPDID": [1927],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_Z,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    "SLE-15-SP7-x86_64": {
        "PDID": [2800, 1712],
        "BETAPDID": [1928],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/sle/15/7/bootstrap/",
    },
    # When adding new SLE15 Service packs, keep in mind the first PDID is for the BaseSystem product (not the base product)!
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
        "PDID": [2282, 2549],
        "BETAPDID": [2552],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.1-s390x": {
        "PDID": [2287, 2550],
        "BETAPDID": [2553],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.1-x86_64": {
        "PDID": [2283, 2551],
        "BETAPDID": [2554],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/1/bootstrap/",
    },
    "SLE-MICRO-5.2-aarch64": {
        "PDID": [2399, 2549],
        "BETAPDID": [2552],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.2-s390x": {
        "PDID": [2400, 2550],
        "BETAPDID": [2553],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.2-x86_64": {
        "PDID": [2401, 2551],
        "BETAPDID": [2554],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/2/bootstrap/",
    },
    "SLE-MICRO-5.3-aarch64": {
        "PDID": [2426, 2549],
        "BETAPDID": [2552],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.3-s390x": {
        "PDID": [2427, 2550],
        "BETAPDID": [2553],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.3-x86_64": {
        "PDID": [2428, 2551],
        "BETAPDID": [2554],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/3/bootstrap/",
    },
    "SLE-MICRO-5.4-aarch64": {
        "PDID": [2572, 2549],
        "BETAPDID": [2552],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.4-s390x": {
        "PDID": [2573, 2550],
        "BETAPDID": [2553],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.4-x86_64": {
        "PDID": [2574, 2551],
        "BETAPDID": [2554],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/4/bootstrap/",
    },
    "SLE-MICRO-5.5-aarch64": {
        "PDID": [2603, 2549],
        "BETAPDID": [2552],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "SLE-MICRO-5.5-s390x": {
        "PDID": [2604, 2550],
        "BETAPDID": [2553],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "SLE-MICRO-5.5-x86_64": {
        "PDID": [2605, 2551],
        "BETAPDID": [2554],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slemicro/5/5/bootstrap/",
    },
    "openSUSE-Leap-15.4-x86_64": {
        "PDID": [2409, 1712],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/4/bootstrap/",
    },
    "openSUSE-Leap-15.4-aarch64": {
        "PDID": [2406, 1709],
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
        "PDID": [2588, 1712],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/5/bootstrap/",
    },
    "openSUSE-Leap-15.5-aarch64": {
        "PDID": [2585, 1709],
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
        "PDID": [2734, 1712],
        "PKGLIST": PKGLIST15_SALT + PKGLIST15_X86_ARM,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/opensuse/15/6/bootstrap/",
    },
    "openSUSE-Leap-15.6-aarch64": {
        "PDID": [2731, 1709],
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
        "PDID": [2697, 2764],
        "BETAPDID": [2767],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.0-s390x": {
        "PDID": [2698, 2765],
        "BETAPDID": [2768],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.0-x86_64": {
        "PDID": [2699, 2763],
        "BETAPDID": [2766],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/0/bootstrap/",
    },
    "SL-MICRO-6.1-aarch64": {
        "PDID": [2775, 2764],
        "BETAPDID": [2767],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-ppc64le": {
        "PDID": [2777, 2899],
        "BETAPDID": [2900],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-s390x": {
        "PDID": [2776, 2765],
        "BETAPDID": [2768],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "SL-MICRO-6.1-x86_64": {
        "PDID": [2774, 2763],
        "BETAPDID": [2766],
        "PKGLIST": PKGLISTMICRO_BUNDLE_ONLY,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/slmicro/6/1/bootstrap/",
    },
    "centos-7-x86_64": {
        "PDID": [-12, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-7-aarch64": {
        "PDID": [-31, 2361],
        "BETAPDID": [2363],
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/7/bootstrap/",
    },
    "centos-8-x86_64": {
        "PDID": [-13, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/centos/8/bootstrap/",
    },
    "centos-8-aarch64": {
        "PDID": [-30, 2362],
        "BETAPDID": [2364],
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
        "PDID": [-14, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-7-aarch64": {
        "PDID": [-28, 2361],
        "BETAPDID": [2363],
        "PKGLIST": RES7 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/7/bootstrap/",
    },
    "oracle-8-x86_64": {
        "PDID": [-17, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/8/bootstrap/",
    },
    "oracle-8-aarch64": {
        "PDID": [-29, 2362],
        "BETAPDID": [2364],
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
        "PDID": [-41, 2543],
        "BETAPDID": [2548],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/oracle/9/bootstrap/",
    },
    "oracle-9-aarch64": {
        "PDID": [-40, 2542],
        "BETAPDID": [2547],
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
        "PDID": [-22, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/amzn/2/bootstrap/",
    },
    "amazonlinux-2-aarch64": {
        "PDID": [-28, 2361],
        "BETAPDID": [2363],
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
    "RHEL7-x86_64": {
        "PDID": [-7, 1683],
        "BETAPDID": [2065],
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "RHEL7-x86_64-uyuni": {
        "BASECHANNEL": "rhel7-pool-uyuni-x86_64",
        "PKGLIST": RES7 + RES7_X86 + RES7REQ,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/7/bootstrap/",
    },
    "SLE-ES8-x86_64": {
        "PDID": [-8, 1921, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "RHEL8-x86_64": {
        "PDID": [-8, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "RHEL8-x86_64-uyuni": {
        "BASECHANNEL": "rhel8-pool-uyuni-x86_64",
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/8/bootstrap/",
    },
    "SUSE-LibertyLinux9-x86_64": {
        "PDID": [-35, 2538, 2543],
        "BETAPDID": [2548],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/res/9/bootstrap/",
    },
    "RHEL9-x86_64": {
        "PDID": [-35, 2543],
        "BETAPDID": [2548],
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
        "PDID": [-23, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/8/bootstrap/",
    },
    "almalinux-8-aarch64": {
        "PDID": [-26, 2362],
        "BETAPDID": [2364],
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
        "PDID": [-38, 2543],
        "BETAPDID": [2548],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/almalinux/9/bootstrap/",
    },
    "almalinux-9-aarch64": {
        "PDID": [-39, 2542],
        "BETAPDID": [2547],
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
        "PDID": [-24, 2007],
        "BETAPDID": [2066],
        "PKGLIST": RES8 + RES8_X86,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/8/bootstrap/",
    },
    "rockylinux-8-aarch64": {
        "PDID": [-27, 2362],
        "BETAPDID": [2364],
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
        "PDID": [-36, 2543],
        "BETAPDID": [2548],
        "PKGLIST": RES9,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/rockylinux/9/bootstrap/",
    },
    "rockylinux-9-aarch64": {
        "PDID": [-37, 2542],
        "BETAPDID": [2547],
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
    "ubuntu-20.04-amd64": {
        "PDID": [-18, 2113],
        "BETAPDID": [2112],
        "PKGLIST": PKGLISTUBUNTU2004,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/20/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-22.04-amd64": {
        "PDID": [-33, 2531],
        "BETAPDID": [2532],
        "PKGLIST": PKGLISTUBUNTU2204,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/22/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-24.04-amd64": {
        "PDID": [-44, 2886],
        "BETAPDID": [2887],
        "PKGLIST": PKGLISTUBUNTU2404,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/24/4/bootstrap/",
        "TYPE": "deb",
    },
    "ubuntu-20.04-amd64-uyuni": {
        "BASECHANNEL": "ubuntu-20.04-pool-amd64-uyuni",
        "PKGLIST": PKGLISTUBUNTU2004,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/ubuntu/20/4/bootstrap/",
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
    "debian11-amd64": {
        "PDID": [-32, 2410],
        "BETAPDID": [2411],
        "PKGLIST": PKGLISTDEBIAN11,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/11/bootstrap/",
        "TYPE": "deb",
    },
    "debian12-amd64": {
        "PDID": [-43, 2677],
        "BETAPDID": [2676],
        "PKGLIST": PKGLISTDEBIAN12,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/12/bootstrap/",
        "TYPE": "deb",
    },
    "debian11-amd64-uyuni": {
        "BASECHANNEL": "debian-11-pool-amd64-uyuni",
        "PKGLIST": PKGLISTDEBIAN11,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/debian/11/bootstrap/",
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
    "openeuler22.03-x86_64-uyuni": {
        "BASECHANNEL": "openeuler2203-x86_64",
        "PKGLIST": OPENEULER2203,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/22.03/bootstrap/",
    },
    "openeuler22.03-aarch64-uyuni": {
        "BASECHANNEL": "openeuler2203-aarch64",
        "PKGLIST": OPENEULER2203,
        "DEST": DOCUMENT_ROOT + "/pub/repositories/openEuler/22.03/bootstrap/",
    },
}
