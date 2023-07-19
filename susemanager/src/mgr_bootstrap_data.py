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


PKGLIST11 = [
    "dbus-1-python",
    "libcurl4",
    "libnl",
    "libopenssl0_9_8",
    "libsqlite3-0",
    "libxml2-python",
    "libzypp",
    "openssl",
    "python",
    "python-dmidecode",
    "python-ethtool",
    "python-openssl",
    "python-xml",
    "rpm-python",
    "satsolver-tools",
    "slang",
    "zypper",
]

ENHANCE11 = [
    "libyaml-0-2",
    "libzmq3",
    "python-backports.ssl_match_hostname",
    "python-certifi",
    "python-curl",
    "python-futures",
    "python-Jinja2",
    "python-MarkupSafe",
    "python-msgpack-python",
    "python-psutil",
    "python-pycrypto",
    "python-pyzmq",
    "python-requests",
    "python-simplejson",
    "python-tornado",
    "python-yaml",
    "salt",
    "salt-minion"
]

PKGLIST11_X86_I586 = [
    "pmtools",
]

PKGLIST12 = [
    "libopenssl1_0_0",
    "openssl",
    "libzypp",
    "rpm-python",
    "libsolv-tools",
    "zypper",
    "cron",
    "cronie"
]

SLE12VENV = [
    "logrotate",
    "libsqlite3-0",
    "venv-salt-minion"
]

ENHANCE12SP1 = [
    "libyui-ncurses-pkg7",
    "libyui-qt-pkg7"
]

RES6 = [
    "salt",
    "salt-minion",
    "python-futures",
    "python-jinja2",
    "python-msgpack-python",
    "python-psutil",
    "python-pycrypto",
    "python-requests",
    "python-setuptools",
    "python-tornado",
    "python-zmq",
    "zeromq",
    "openssl",
    "python-certifi",
    "PyYAML",
    "python-markupsafe",
    "python-urllib3",
    "libyaml",
    "python-chardet",
    "python-six",
    "yum-plugin-security",
    "yum",
    "rpm-python",
    "redhat-rpm-config",
    "openssh-clients",
]

RES7 = [
    "logrotate",
    "venv-salt-minion",
]

RES7_X86 = [
    "dmidecode"
]

RES8 = [
    "redhat-rpm-config",
    "salt",
    "salt-minion",
    "python3-salt",
    "python3-babel",
    "python3-msgpack",
    "python3-tornado",
    "python3-zmq",
    "python3-jinja2",
    "python3-m2crypto",
    "python3-markupsafe",
    "python3-psutil",
    "python3-pyyaml",
    "python3-requests",
    "openpgm",
    "zeromq",
    "python3-urllib3",
    "python3-idna",
    "python3-chardet",
    "python3-pysocks",
    "python3-pytz",
    "python3-setuptools",
    "python3-distro",
    "python3-immutables",
    "python3-contextvars",
    "python3-looseversion",
    "python3-jmespath",
    "venv-salt-minion",
]

RES8_X86 = [
    "dmidecode"
]

RES9 = [
    "venv-salt-minion", 
]

OPENEULER2203 = [
        "venv-salt-minion",
]

PKGLISTUMBLEWEED_SALT_NO_BUNDLE = [
    "libgomp1",
    "libmpdec3",
    "libpgm-5_2-0",
    "libpython3_10-1_0",
    "librpmbuild9",
    "libsodium23",
    "libunwind8",
    "libyaml-0-2",
    "libzmq5",
    "python3-salt",
    "python310",
    "python310-Jinja2",
    "python310-M2Crypto",
    "python310-MarkupSafe",
    "python310-PyYAML",
    "python310-apipkg",
    "python310-base",
    "python310-certifi",
    "python310-cffi",
    "python310-charset-normalizer",
    "python310-contextvars",
    "python310-cryptography",
    "python310-distro",
    "python310-idna",
    "python310-immutables",
    "python310-iniconfig",
    "python310-msgpack",
    "python310-psutil",
    "python310-py",
    "python310-pyOpenSSL",
    "python310-pycparser",
    "python310-pyzmq",
    "python310-requests",
    "python310-rpm",
    "python310-six",
    "python310-urllib3",
    "python310-zypp-plugin",
    "salt",
    "salt-minion",
    "salt-transactional-update",
    "update-alternatives",
]

PKGLIST15_SALT_NO_BUNDLE = [
    "hostname",
    "iproute2",
    "libmnl0",
    "libxtables12",
    "libpgm-5_2-0",
    "libpython3_6m1_0",
    "libsodium23",
    "libzmq5",
    "logrotate",
    "net-tools",
    "openssl",
    "python3",
    "python3-base",
    "python3-appdirs",
    "python3-asn1crypto",
    "python3-Babel",
    "python3-certifi",
    "python3-cffi",
    "python3-chardet",
    "python3-cryptography",
    "python3-distro",
    "python3-idna",
    "python3-Jinja2",
    "python3-MarkupSafe",
    "python3-M2Crypto",
    "python3-msgpack",
    "python3-ordered-set*",
    "python3-packaging",
    "python3-psutil",
    "python3-py",
    "python3-pyasn1",
    "python3-pycparser",
    "python3-pyparsing",
    "python3-pytz",
    "python3-pyOpenSSL",
    "python3-PyYAML",
    "python3-pyzmq",
    "python3-requests",
    "python3-rpm",
    "python3-setuptools",
    "python3-simplejson",
    "python3-six",
    "python3-urllib3",
    "python3-immutables*",
    "python3-contextvars*",
    "python3-zypp-plugin",
    "timezone",
    "salt",
    "python3-salt",
    "salt-minion",
    "python3-apipkg*",
    "python3-iniconfig*",
    "python3-looseversion",
    "python3-jmespath",
    "xz",
]

PKGLIST15_SALT = PKGLIST15_SALT_NO_BUNDLE + [
    "venv-salt-minion",
]

PKGLIST15_SALT_OPT_BUNDLE = PKGLIST15_SALT_NO_BUNDLE + [
    "venv-salt-minion*",
]

PKGLISTMICRO_BUNDLE_ONLY = [
    "venv-salt-minion",
]

ONLYSLE15 = [
    "gio-branding-SLE",
]

PKGLIST15_X86_ARM = [
    "dmidecode",
    "libunwind",
]

PKGLIST15_PPC = [
    "libunwind",
]

PKGLIST15_Z = [
]

PKGLISTUBUNTU1604 = [
    "libsodium18",
    "dctrl-tools",
    "libzmq5",
    "python-chardet",
    "python-croniter",
    "python-crypto",
    "python-dateutil",
    "python-enum34",
    "python-ipaddress",
    "python-jinja2",
    "python-markupsafe",
    "python-minimal",
    "python-msgpack",
    "python-openssl",
    "python-pkg-resources",
    "python-psutil",
    "python-requests",
    "python-six",
    "python-systemd",
    "python-tornado",
    "python-tz",
    "python-urllib3",
    "python-yaml",
    "python-zmq",
    "python-pycurl",
    "salt-common",
    "salt-minion",
    "dmidecode",
]

PKGLISTUBUNTU1804 = [
    "dctrl-tools",
    "javascript-common",
    "libjs-jquery",
    "libjs-sphinxdoc",
    "libjs-underscore",
    "libnorm1",
    "libpgm-5.2-0",
    "libpython3.6-stdlib",
    "libpython3.6-minimal",
    "libpython3.6-stdlib",
    "libsodium23",
    "libzmq5",
    "python3",
    "python3-apt",
    "python3-asn1crypto",
    "python3-certifi",
    "python3-cffi-backend",
    "python3-chardet",
    "python3-croniter",
    "python3-crypto",
    "python3-pycryptodome",
    "python3-cryptography",
    "python3-dateutil",
    "python3-idna",
    "python3-jinja2",
    "python3-markupsafe",
    "python3-minimal",
    "python3-msgpack",
    "python3-openssl",
    "python3-pkg-resources",
    "python3-psutil",
    "python3-requests",
    "python3-singledispatch",
    "python3-six",
    "python3-systemd",
    "python3-tornado",
    "python3-tz",
    "python3-urllib3",
    "python3-yaml",
    "python3-zmq",
    "python3.6",
    "python3.6-minimal",
    "salt-common",
    "salt-minion",
    "dmidecode",
    "bsdmainutils",
    "debconf-utils",
    "iso-codes",
    "python-apt-common",
    "python3-distro",
    "python3-gnupg",
    "gnupg",
    "python3-immutables",
    "python3-contextvars",
    "python3-looseversion",
    "python3-jmespath",
    "venv-salt-minion",
]

PKGLISTUBUNTU2004 = [
    "dctrl-tools",
    "libnorm1",
    "libpgm-5.2-0",
    "libzmq5",
    "python3-crypto",
    "python3-dateutil",
    "python3-distro",
    "python3-jinja2",
    "python3-markupsafe",
    "python3-msgpack",
    "python3-psutil",
    "python3-pycryptodome",
    "python3-zmq",
    "python3-gnupg",
    "python3-looseversion",
    "python3-jmespath",
    "salt-common",
    "salt-minion",
    "gnupg",
    "venv-salt-minion",
]

PKGLISTUBUNTU2204 = [
    "venv-salt-minion",
]

PKGLISTDEBIAN9 = [
    "apt-transport-https",
    "bsdmainutils",
    "dctrl-tools",
    "debconf-utils",
    "gnupg1",
    "gnupg1-curl",
    "gnupg1-l10n",
    "javascript-common",
    "libjs-jquery",
    "libjs-sphinxdoc",
    "libjs-underscore",
    "libpgm-5.2-0",
    "libpython-stdlib",
    "libpython2.7-minimal",
    "libpython2.7-stdlib",
    "libzmq5",
    "libsodium18",
    "libyaml-0-2",
    "libcurl3-gnutls",
    "python",
    "python-apt",
    "python-backports-abc",
    "python-certifi",
    "python-cffi-backend",
    "python-chardet",
    "python-concurrent.futures",
    "python-croniter",
    "python-crypto",
    "python-cryptography",
    "python-dateutil",
    "python-enum34",
    "python-gnupg",
    "python-idna",
    "python-ipaddress",
    "python-jinja2",
    "python-mako",
    "python-markupsafe",
    "python-minimal",
    "python-msgpack",
    "python-openssl",
    "python-pkg-resources",
    "python-psutil",
    "python-requests",
    "python-singledispatch",
    "python-six",
    "python-systemd",
    "python-tornado",
    "python-tz",
    "python-urllib3",
    "python-yaml",
    "python-zmq",
    "python2.7",
    "python2.7-minimal",
    "salt-common",
    "salt-minion",
    "dmidecode",
    "gnupg",
    "gnupg1",
    "venv-salt-minion*",
]


PKGLISTDEBIAN10 = [
    "dctrl-tools",
    "debconf-utils",
    "dirmngr",
    "distro-info-data",
    "dmidecode",
    "gnupg",
    "gnupg-l10n",
    "gnupg-utils",
    "gpg",
    "gpg-agent",
    "gpgconf",
    "gpgsm",
    "gpg-wks-client",
    "gpg-wks-server",
    "iso-codes",
    "libassuan0",
    "libksba8",
    "libldap-2.4-2",
    "libldap-common",
    "libnorm1",
    "libnpth0",
    "libpgm-5.2-0",
    "libsasl2-2",
    "libsasl2-modules",
    "libsasl2-modules-db",
    "libsodium23",
    "libyaml-0-2",
    "libzmq5",
    "lsb-release",
    "pinentry-curses",
    "python3-apt",
    "python3-certifi",
    "python3-chardet",
    "python3-croniter",
    "python3-crypto",
    "python3-pycryptodome",
    "python3-dateutil",
    "python3-distro",
    "python3-idna",
    "python3-jinja2",
    "python3-markupsafe",
    "python3-msgpack",
    "python3-pkg-resources",
    "python3-psutil",
    "python3-requests",
    "python3-six",
    "python3-systemd",
    "python3-tz",
    "python3-urllib3",
    "python3-yaml",
    "python3-zmq",
    "python-apt-common",
    "salt-common",
    "salt-minion",
    "gnupg",
    "venv-salt-minion",
    "python3-gnupg",
    "python3-looseversion",
    "python3-jmespath",
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
    "venv-salt-minion"
]

PKGLISTASTRALINUXOREL = [
    "dctrl-tools",
    "dirmngr",
    "iso-codes",
    "libjs-jquery",
    "libjs-sphinxdoc",
    "libjs-underscore",
    "libpgm-5.2-0",
    "libpython-stdlib",
    "libpython2.7-minimal",
    "libpython2.7-stdlib",
    "libsodium18",
    "libyaml-0-2",
    "libzmq5",
    "python",
    "python-apt",
    "python-apt-common",
    "python-backports-abc",
    "python-cffi-backend",
    "python-chardet",
    "python-concurrent.futures",
    "python-crypto",
    "python-cryptography",
    "python-dateutil",
    "python-enum34",
    "python-idna",
    "python-ipaddress",
    "python-jinja2",
    "python-markupsafe",
    "python-minimal",
    "python-msgpack",
    "python-openssl",
    "python-pkg-resources",
    "python-pyasn1",
    "python-psutil",
    "python-requests",
    "python-setuptools",
    "python-singledispatch",
    "python-six",
    "python-systemd",
    "python-tornado",
    "python-urllib3",
    "python-yaml",
    "python-zmq",
    "python2.7",
    "python2.7-minimal",
    "salt-common",
    "salt-minion",
    "gnupg",
]

DATA = {
    'SLE-11-SP1-i586' : {
        'PDID' : 684, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-ia64' : {
        'PDID' : 1033, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-ppc64' : {
        'PDID' : 940, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-s390x' : {
        'PDID' : 745, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-x86_64' : {
        'PDID' : 769, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP2-i586' : {
        'PDID' : 811, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-ia64' : {
        'PDID' : 1034, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-ppc64' : {
        'PDID' : 946, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-s390x' : {
        'PDID' : 755, 'PKGLIST' : PKGLIST11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-x86_64' : {
        'PDID' : 690, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP3-i586' : {
        'PDID' : 793, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-ia64' : {
        'PDID' : 1037, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-ppc64' : {
        'PDID' : 949, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-s390x' : {
        'PDID' : 693, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-x86_64' : {
        'PDID' : 814, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP4-i586' : {
        'PDID' : [1299], 'BETAPDID' : [2071], 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-ia64' : {
        'PDID' : 1302, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-ppc64' : {
        'PDID' : [1301], 'BETAPDID' : [2072], 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-s390x' : {
        'PDID' : [1303], 'BETAPDID' : [2073], 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-x86_64' : {
        'PDID' : [1300], 'BETAPDID' : [2074], 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLES4SAP-11-SP1-x86_64' : {
        'PDID' : 1129, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLES4SAP-11-SP2-x86_64' : {
        'PDID' : 1130, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLES4SAP-11-SP3-x86_64' : {
        'PDID' : 1131, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLES4SAP-11-SP4-x86_64' : {
        'PDID' : [1329], 'BETAPDID' : [2074], 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLES4SAP-11-SP4-ppc64' : {
        'PDID' : [1331], 'BETAPDID' : [2072], 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-12-ppc64le' : {
        'PDID' : 1116, 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLE-12-s390x' : {
        'PDID' : 1115, 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLE-12-x86_64' : {
        'PDID' : 1117, 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLES4SAP-12-x86_64' : {
        'PDID' : 1319, 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLE-12-SP1-ppc64le' : {
        'PDID' : 1334, 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLE-12-SP1-s390x' : {
        'PDID' : [1335, 1535], 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLE-12-SP1-x86_64' : {
        'PDID' : [1322, 1533], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLES4SAP-12-SP1-ppc64le' : {
        'PDID' : 1437, 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLES4SAP-12-SP1-x86_64' : {
        'PDID' : 1346, 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/1/bootstrap/'
    },
    'RES6-x86_64' : {
        'PDID' : [1138], 'BETAPDID' : [2064], 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/6/bootstrap/'
    },
    'RES7-x86_64' : {
        'PDID' : [1251], 'BETAPDID' : [2065], 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/7/bootstrap/'
    },
    'SLE-12-SP2-aarch64' : {
        'PDID' : 1375, 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES_RPI-12-SP2-aarch64' : {
        'PDID' : 1418, 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-ppc64le' : {
        'PDID' : [1355, 1737], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-s390x' : {
        'PDID' : [1356, 1738], 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-x86_64' : {
        'PDID' : [1357, 1739], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES4SAP-12-SP2-x86_64' : {
        'PDID' : 1414, 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES4SAP-12-SP2-ppc64le' : {
        'PDID' : 1521, 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP3-aarch64' : {
        'PDID' : [1424, 2002], 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-ppc64le' : {
        'PDID' : [1422, 1930], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-s390x' : {
        'PDID' : [1423, 1931], 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-x86_64' : {
        'PDID' : [1421, 1932], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLED-12-SP3-x86_64' : {
        'PDID' : [1425], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLES4SAP-12-SP3-x86_64' : {
        'PDID' : 1426, 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLES4SAP-12-SP3-ppc64le' : {
        'PDID' : 1572, 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP4-aarch64' : {
        'PDID' : [1628, 2114], 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-ppc64le' : {
        'PDID' : [1626, 2115], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-s390x' : {
        'PDID' : [1627, 2116], 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-x86_64' : {
        'PDID' : [1625, 2117], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLED-12-SP4-x86_64' : {
        'PDID' : [1629], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLES4SAP-12-SP4-x86_64' : {
        'PDID' : [1755], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLES4SAP-12-SP4-ppc64le' : {
        'PDID' : [1754], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE4HPC-12-SP4-x86_64' : {
        'PDID' : [1759], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE4HPC-12-SP4-aarch64' : {
        'PDID' : [1758], 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP5-aarch64' : {
        'PDID' : [1875], 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-ppc64le' : {
        'PDID' : [1876], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-s390x' : {
        'PDID' : [1877], 'BETAPDID' : [1746], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-x86_64' : {
        'PDID' : [1878], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLES4SAP-12-SP5-x86_64' : {
        'PDID' : [1880], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLES4SAP-12-SP5-ppc64le' : {
        'PDID' : [1879], 'BETAPDID' : [1745], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE4HPC-12-SP5-x86_64' : {
        'PDID' : [1873], 'BETAPDID' : [1747], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE4HPC-12-SP5-aarch64' : {
        'PDID' : [1872], 'BETAPDID' : [1744], 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'OES2018-x86_64' : {
        'PDID' : 45, 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/2/bootstrap/'
    },
    'OES2018-SP1-x86_64' : {
        'PDID' : 46, 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/3/bootstrap/'
    },
    'OES2018-SP2-x86_64' : {
        'PDID' : -9, 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'OES2018-SP3-x86_64' : {
        'PDID' : -21, 'PKGLIST' : PKGLIST12 + SLE12VENV + ENHANCE12SP1,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/12/5/bootstrap/'
    },
    'OES2023' : {
        'PDID' : -34, 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-15-aarch64' : {
        'PDID' : [1589, 2053, 1709], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-ppc64le' : {
        'PDID' : [1588, 2054, 1710], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-s390x' : {
        'PDID' : [1587, 2055, 1711], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-x86_64' : {
        'PDID' : [1576, 2056, 1712], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLES4SAP-15-ppc64le' : {
        'PDID' : [1588, 1613, 1710], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLES4SAP-15-x86_64' : {
        'PDID' : [1576, 1612, 1712], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-SP1-aarch64' : {
        'PDID' : [1769, 1709, 2216], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-ppc64le' : {
        'PDID' : [1770, 1710, 2217], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-s390x' : {
        'PDID' : [1771, 1711, 2218], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-x86_64' : {
        'PDID' : [1772, 1712, 2219], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/1/bootstrap/'
    },
    'SUMA-40-PROXY-x86_64' : {
        'PDID' : [1772, 1908], 'BETAPDID' : [], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP2-aarch64' : {
        'PDID' : [1943, 1709, 2372], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-ppc64le' : {
        'PDID' : [1944, 1710, 2373], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-s390x' : {
        'PDID' : [1945, 1711, 2374], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-x86_64' : {
        'PDID' : [1946, 1712, 2375], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/2/bootstrap/'
    },
    'SUMA-41-PROXY-x86_64' : {
        'PDID' : [1946, 2015], 'BETAPDID' : [], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP3-aarch64' : {
        'PDID' : [2142, 1709, 2567], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-ppc64le' : {
        'PDID' : [2143, 1710, 2568], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-s390x' : {
        'PDID' : [2144, 1711, 2569], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-x86_64' : {
        'PDID' : [2145, 1712, 2570], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/3/bootstrap/'
    },
    'SUMA-42-PROXY-x86_64' : {
        'PDID' : [2145, 2225], 'BETAPDID' : [], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT_OPT_BUNDLE + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP4-aarch64' : {
        'PDID' : [2296, 1709], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-15-SP4-ppc64le' : {
        'PDID' : [2297, 1710], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-15-SP4-s390x' : {
        'PDID' : [2298, 1711], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-15-SP4-x86_64' : {
        'PDID' : [2299, 1712], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-15-SP5-aarch64' : {
        'PDID' : [2471, 1709], 'BETAPDID' : [1925], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/5/bootstrap/'
    },
    'SLE-15-SP5-ppc64le' : {
        'PDID' : [2472, 1710], 'BETAPDID' : [1926], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/5/bootstrap/'
    },
    'SLE-15-SP5-s390x' : {
        'PDID' : [2473, 1711], 'BETAPDID' : [1927], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/5/bootstrap/'
    },
    'SLE-15-SP5-x86_64' : {
        'PDID' : [2474, 1712], 'BETAPDID' : [1928], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/5/bootstrap/'
    },
    # When adding new SLE15 Service packs, keep in mind the first PDID is for the BaseSystem product (not the base product)!
    'SUMA-43-PROXY-x86_64' : {
        'PDID' : [2299, 2384], 'BETAPDID' : [], 'PKGLIST' :  ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/sle/15/4/bootstrap/'
    },
    'SLE-MICRO-5.1-aarch64' : {
        'PDID' : [2282, 2549], 'BETAPDID' : [2552], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/1/bootstrap/'
    },
    'SLE-MICRO-5.1-s390x' : {
        'PDID' : [2287, 2550], 'BETAPDID' : [2553], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/1/bootstrap/'
    },
    'SLE-MICRO-5.1-x86_64' : {
        'PDID' : [2283, 2551], 'BETAPDID' : [2554], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/1/bootstrap/'
    },
    'SLE-MICRO-5.2-aarch64' : {
        'PDID' : [2399, 2549], 'BETAPDID' : [2552], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/2/bootstrap/'
    },
    'SLE-MICRO-5.2-s390x' : {
        'PDID' : [2400, 2550], 'BETAPDID' : [2553], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/2/bootstrap/'
    },
    'SLE-MICRO-5.2-x86_64' : {
        'PDID' : [2401, 2551], 'BETAPDID' : [2554], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/2/bootstrap/'
    },
    'SLE-MICRO-5.3-aarch64' : {
        'PDID' : [2426, 2549], 'BETAPDID' : [2552], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/3/bootstrap/'
    },
    'SLE-MICRO-5.3-s390x' : {
        'PDID' : [2427, 2550], 'BETAPDID' : [2553], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/3/bootstrap/'
    },
    'SLE-MICRO-5.3-x86_64' : {
        'PDID' : [2428, 2551], 'BETAPDID' : [2554], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/3/bootstrap/'
    },
    'SLE-MICRO-5.4-aarch64' : {
        'PDID' : [2572, 2549], 'BETAPDID' : [2552], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/4/bootstrap/'
    },
    'SLE-MICRO-5.4-s390x' : {
        'PDID' : [2573, 2550], 'BETAPDID' : [2553], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/4/bootstrap/'
    },
    'SLE-MICRO-5.4-x86_64' : {
        'PDID' : [2574, 2551], 'BETAPDID' : [2554], 'PKGLIST' : PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/slemicro/5/4/bootstrap/'
    },
    'openSUSE-Leap-15-x86_64' : {
        'BASECHANNEL' : 'opensuse_leap15_0-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/0/bootstrap/'
    },
    'openSUSE-Leap-15.1-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_1-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/1/bootstrap/'
    },
    'openSUSE-Leap-15.1-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_1-aarch64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/1/bootstrap/'
    },
    'openSUSE-Leap-15.1-x86_64' : {
        'PDID' : [1929, 1712], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/1/bootstrap/'
    },
    'openSUSE-Leap-15.2-x86_64' : {
        'PDID' : [2001, 1712], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/2/bootstrap/'
    },
    'openSUSE-Leap-15.2-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_2-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/2/bootstrap/'
    },
    'openSUSE-Leap-15.2-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_2-aarch64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/2/bootstrap/'
    },
    'openSUSE-Leap-15.3-x86_64' : {
        'PDID' : [2236, 1712], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/3/bootstrap/'
    },
    'openSUSE-Leap-15.3-aarch64' : {
        'PDID' : [2233, 1709], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/3/bootstrap/'
    },
    'openSUSE-Leap-15.3-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_3-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/3/bootstrap/'
    },
    'openSUSE-Leap-15.3-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_3-aarch64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/3/bootstrap/'
    },
    'openSUSE-Leap-15.4-x86_64' : {
        'PDID' : [2409, 1712], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/4/bootstrap/'
    },
    'openSUSE-Leap-15.4-aarch64' : {
        'PDID' : [2406, 1709], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/4/bootstrap/'
    },
    'openSUSE-Leap-15.4-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_4-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/4/bootstrap/'
    },
    'openSUSE-Leap-15.4-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_4-aarch64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/4/bootstrap/'
    },
    'openSUSE-Leap-15.5-x86_64' : {
        'PDID' : [2588, 1712], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/5/bootstrap/'
    },
    'openSUSE-Leap-15.5-aarch64' : {
        'PDID' : [2585, 1709], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/5/bootstrap/'
    },
    'openSUSE-Leap-15.5-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_5-x86_64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/5/bootstrap/'
    },
    'openSUSE-Leap-15.5-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_5-aarch64', 'PKGLIST' :  PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensuse/15/5/bootstrap/'
    },
    'openSUSE-Leap-Micro-5.3-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_micro5_3-x86_64', 'PKGLIST' :  PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicro/5/3/bootstrap/'
    },
    'openSUSE-Leap-Micro-5.3-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_micro5_3-aarch64', 'PKGLIST' :  PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicro/5/3/bootstrap/'
    },
    'openSUSE-Leap-Micro-5.4-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_micro5_4-x86_64', 'PKGLIST' :  PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicro/5/4/bootstrap/'
    },
    'openSUSE-Leap-Micro-5.4-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_micro5_4-aarch64', 'PKGLIST' :  PKGLISTMICRO_BUNDLE_ONLY,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicro/5/4/bootstrap/'
    },
    'openSUSE-MicroOS-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_microos-x86_64', 'PKGLIST' : PKGLISTUMBLEWEED_SALT_NO_BUNDLE,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicroos/latest/0/bootstrap/'
    },
    'openSUSE-MicroOS-aarch64-uyuni' : {
        'BASECHANNEL' : 'opensuse_microos-aarch64', 'PKGLIST' : PKGLISTUMBLEWEED_SALT_NO_BUNDLE,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/opensusemicroos/latest/0/bootstrap/'
    },
    'centos-6-x86_64' : {
        'PDID' : [-11, 1682], 'BETAPDID' : [2064], 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/6/bootstrap/'
    },
    'centos-7-x86_64' : {
        'PDID' : [-12, 1683], 'BETAPDID' : [2065], 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/7/bootstrap/'
    },
    'centos-7-aarch64' : {
        'PDID' : [-31, 2361], 'BETAPDID' : [2363], 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/7/bootstrap/'
    },
    'centos-8-x86_64' : {
        'PDID' : [-13, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/8/bootstrap/'
    },
    'centos-8-aarch64' : {
        'PDID' : [-30, 2362], 'BETAPDID' : [2364], 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/8/bootstrap/'
    },
    'centos-6-x86_64-uyuni' : {
        'BASECHANNEL' : 'centos6-x86_64', 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/6/bootstrap/'
    },
    'centos-7-x86_64-uyuni' : {
        'BASECHANNEL' : 'centos7-x86_64', 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/7/bootstrap/'
    },
    'centos-7-ppc64le-uyuni' : {
        'BASECHANNEL' : 'centos7-ppc64le', 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/7/bootstrap/'
    },
    'centos-7-aarch64-uyuni' : {
        'BASECHANNEL' : 'centos7-aarch64', 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/7/bootstrap/'
    },
    'centos-8-x86_64-uyuni' : {
        'BASECHANNEL' : 'centos8-x86_64', 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/8/bootstrap/'
    },
    'centos-8-ppc64le-uyuni' : {
        'BASECHANNEL' : 'centos8-ppc64le', 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/8/bootstrap/'
    },
    'centos-8-aarch64-uyuni' : {
        'BASECHANNEL' : 'centos8-aarch64', 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/centos/8/bootstrap/'
    },
    'oracle-6-x86_64' : {
        'PDID' : [-15, 1682], 'BETAPDID' : [2064], 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/6/bootstrap/'
    },
    'oracle-7-x86_64' : {
        'PDID' : [-14, 1683], 'BETAPDID' : [2065], 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/7/bootstrap/'
    },
    'oracle-7-aarch64' : {
        'PDID' : [-28, 2361], 'BETAPDID' : [2363], 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/7/bootstrap/'
    },
    'oracle-8-x86_64' : {
        'PDID' : [-17, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/8/bootstrap/'
    },
    'oracle-8-aarch64' : {
        'PDID' : [-29, 2362], 'BETAPDID' : [2364], 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/8/bootstrap/'
    },
    'oracle-6-x86_64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux6-x86_64', 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/6/bootstrap/'
    },
    'oracle-7-x86_64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux7-x86_64', 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/7/bootstrap/'
    },
    'oracle-7-aarch64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux7-aarch64', 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/7/bootstrap/'
    },
    'oracle-8-x86_64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux8-x86_64', 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/8/bootstrap/'
    },
    'oracle-8-aarch64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux8-aarch64', 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/8/bootstrap/'
    },
    'oracle-9-x86_64' : {
        'PDID' : [-41, 2543], 'BETAPDID' : [2548], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/9/bootstrap/'
    },
    'oracle-9-aarch64' : {
        'PDID' : [-40, 2542], 'BETAPDID' : [2547], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/9/bootstrap/'
    },
    'oracle-9-x86_64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux9-x86_64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/9/bootstrap/'
    },
    'oracle-9-aarch64-uyuni' : {
        'BASECHANNEL' : 'oraclelinux9-aarch64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/oracle/9/bootstrap/'
    },
    'amazonlinux-2-x86_64' : {
        'PDID' : [-22, 1683], 'BETAPDID' : [2065], 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/amzn/2/bootstrap/'
    },
    'amazonlinux-2-aarch64' : {
        'PDID' : [-28, 2361], 'BETAPDID' : [2363], 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/amzn/2/bootstrap/'
    },
    'amazonlinux-2-x86_64-uyuni' : {
        'BASECHANNEL' : 'amazonlinux2-core-x86_64', 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/amzn/2/bootstrap/'
    },
    'amazonlinux-2-aarch64-uyuni' : {
        'BASECHANNEL' : 'amazonlinux2-core-aarch64', 'PKGLIST' : RES7,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/amzn/2/bootstrap/'
    },
    'RHEL6-x86_64' : {
        'PDID' : [-5, 1682], 'BETAPDID' : [2064], 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/6/bootstrap/'
    },
    'RHEL6-i386' : {
        'PDID' : [-6, 1681], 'BETAPDID' : [2063], 'PKGLIST' : RES6,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/6/bootstrap/'
    },
    'RHEL7-x86_64' : {
        'PDID' : [-7, 1683], 'BETAPDID' : [2065], 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/7/bootstrap/'
    },
    'RHEL7-x86_64-uyuni' : {
        'BASECHANNEL' : 'rhel7-pool-x86_64', 'PKGLIST' : RES7 + RES7_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/7/bootstrap/'
    },
    'SLE-ES8-x86_64' : {
        'PDID' : [-8, 1921, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/8/bootstrap/'
    },
    'RHEL8-x86_64' : {
        'PDID' : [-8, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/8/bootstrap/'
    },
    'RHEL8-x86_64-uyuni' : {
        'BASECHANNEL' : 'rhel8-pool-x86_64', 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/8/bootstrap/'
    },
    'SUSE-LibertyLinux9-x86_64' : {
        'PDID' : [-35, 2538, 2543], 'BETAPDID' : [2548], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/9/bootstrap/'
    },
    'RHEL9-x86_64' : {
        'PDID' : [-35, 2543], 'BETAPDID' : [2548], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/9/bootstrap/'
    },
    'RHEL9-x86_64-uyuni' : {
        'BASECHANNEL' : 'rhel9-pool-x86_64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/res/9/bootstrap/'
    },
    'alibaba-2-x86_64-uyuni': {
        'BASECHANNEL': 'alibaba-2-x86_64', 'PKGLIST': RES7 + RES7_X86,
        'DEST': DOCUMENT_ROOT + '/pub/repositories/alibaba/2/bootstrap/'
    },
    'alibaba-2-aarch64-uyuni': {
        'BASECHANNEL': 'alibaba-2-aarch64', 'PKGLIST': RES7,
        'DEST': DOCUMENT_ROOT + '/pub/repositories/alibaba/2/bootstrap/'
    },
    'almalinux-8-x86_64' : {
        'PDID' : [-23, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/8/bootstrap/'
    },
    'almalinux-8-aarch64' : {
        'PDID' : [-26, 2362], 'BETAPDID' : [2364], 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/8/bootstrap/'
    },
    'almalinux-8-x86_64-uyuni' : {
        'BASECHANNEL' : 'almalinux8-x86_64', 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/8/bootstrap/'
    },
    'almalinux-8-aarch64-uyuni' : {
        'BASECHANNEL' : 'almalinux8-aarch64', 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/8/bootstrap/'
    },
    'almalinux-9-x86_64' : {
        'PDID' : [-38, 2543], 'BETAPDID' : [2548], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'almalinux-9-aarch64' : {
        'PDID' : [-39, 2542], 'BETAPDID' : [2547], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'almalinux-9-x86_64-uyuni' : {
        'BASECHANNEL' : 'almalinux9-x86_64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'almalinux-9-aarch64-uyuni' : {
        'BASECHANNEL' : 'almalinux9-aarch64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'almalinux-9-ppc64le-uyuni' : {
        'BASECHANNEL' : 'almalinux9-ppc64le', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'almalinux-9-s390x-uyuni' : {
        'BASECHANNEL' : 'almalinux9-s390x', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/almalinux/9/bootstrap/'
    },
    'rockylinux-8-x86_64' : {
        'PDID' : [-24, 2007], 'BETAPDID' : [2066], 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/8/bootstrap/'
    },
    'rockylinux-8-aarch64' : {
        'PDID' : [-27, 2362], 'BETAPDID' : [2364], 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/8/bootstrap/'
    },
    'rockylinux-8-x86_64-uyuni' : {
        'BASECHANNEL' : 'rockylinux8-x86_64', 'PKGLIST' : RES8 + RES8_X86,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/8/bootstrap/'
    },
    'rockylinux-8-aarch64-uyuni' : {
        'BASECHANNEL' : 'rockylinux8-aarch64', 'PKGLIST' : RES8,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/8/bootstrap/'
    },
    'rockylinux-9-x86_64' : {
        'PDID' : [-36, 2543], 'BETAPDID' : [2548], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'rockylinux-9-aarch64' : {
        'PDID' : [-37, 2542], 'BETAPDID' : [2547], 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'rockylinux-9-x86_64-uyuni' : {
        'BASECHANNEL' : 'rockylinux9-x86_64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'rockylinux-9-aarch64-uyuni' : {
        'BASECHANNEL' : 'rockylinux9-aarch64', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'rockylinux-9-ppc64le-uyuni' : {
        'BASECHANNEL' : 'rockylinux9-ppc64le', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'rockylinux-9-s390x-uyuni' : {
        'BASECHANNEL' : 'rockylinux9-s390x', 'PKGLIST' : RES9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/rockylinux/9/bootstrap/'
    },
    'ubuntu-16.04-amd64' : {
        'PDID' : [-2, 1917], 'BETAPDID' : [2061], 'PKGLIST' : PKGLISTUBUNTU1604,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/16/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-18.04-amd64' : {
        'PDID' : [-1, 1918], 'BETAPDID' : [2062], 'PKGLIST' : PKGLISTUBUNTU1804,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/18/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-20.04-amd64' : {
        'PDID' : [-18, 2113], 'BETAPDID' : [2112], 'PKGLIST' : PKGLISTUBUNTU2004,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/20/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-22.04-amd64' : {
        'PDID' : [-33, 2531], 'BETAPDID' : [2532], 'PKGLIST' : PKGLISTUBUNTU2204,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/22/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-16.04-amd64-uyuni' : {
        'BASECHANNEL' : 'ubuntu-16.04-pool-amd64-uyuni', 'PKGLIST' : PKGLISTUBUNTU1604,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/16/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-18.04-amd64-uyuni' : {
        'BASECHANNEL' : 'ubuntu-18.04-pool-amd64-uyuni', 'PKGLIST' : PKGLISTUBUNTU1804,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/18/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-20.04-amd64-uyuni' : {
        'BASECHANNEL' : 'ubuntu-20.04-pool-amd64-uyuni', 'PKGLIST' : PKGLISTUBUNTU2004,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/20/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-22.04-amd64-uyuni' : {
        'BASECHANNEL' : 'ubuntu-22.04-pool-amd64-uyuni', 'PKGLIST' : PKGLISTUBUNTU2204,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/ubuntu/22/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'debian9-amd64' : {
        'PDID' : [-19, 2208], 'BETAPDID' : [2209], 'PKGLIST' : PKGLISTDEBIAN9,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/debian/9/bootstrap/',
        'TYPE' : 'deb'
    },
    'debian10-amd64' : {
        'PDID' : [-20, 2210], 'BETAPDID' : [2211], 'PKGLIST' : PKGLISTDEBIAN10,
        'DEST' : DOCUMENT_ROOT + '/pub/repositories/debian/10/bootstrap/',
        'TYPE' : 'deb'
    },
    'debian11-amd64' : {
        'PDID' : [-32, 2410], 'BETAPDID' : [2411], 'PKGLIST' : PKGLISTDEBIAN11,
        'DEST' : '/srv/www/htdocs/pub/repositories/debian/11/bootstrap/',
        'TYPE' : 'deb'
    },
    'debian9-amd64-uyuni' : {
         'BASECHANNEL' : 'debian-9-pool-amd64-uyuni', 'PKGLIST' : PKGLISTDEBIAN9,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/debian/9/bootstrap/',
         'TYPE' : 'deb'
     },
     'debian10-amd64-uyuni' : {
         'BASECHANNEL' : 'debian-10-pool-amd64-uyuni', 'PKGLIST' : PKGLISTDEBIAN10,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/debian/10/bootstrap/',
         'TYPE' : 'deb'
     },
     'debian11-amd64-uyuni' : {
         'BASECHANNEL' : 'debian-11-pool-amd64-uyuni', 'PKGLIST' : PKGLISTDEBIAN11,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/debian/11/bootstrap/',
         'TYPE' : 'deb'
     },
     'astralinux-orel-amd64': {
         'BASECHANNEL' : 'astralinux-orel-pool-amd64', 'PKGLIST' : PKGLISTASTRALINUXOREL,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/astra/orel/bootstrap/',
         'TYPE' : 'deb'
     },
     'openeuler22.03-x86_64-uyuni': {
         'BASECHANNEL' : 'openeuler2203-x86_64', 'PKGLIST' : OPENEULER2203,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/openEuler/22.03/bootstrap/'
     },
     'openeuler22.03-aarch64-uyuni': {
         'BASECHANNEL' : 'openeuler2203-aarch64', 'PKGLIST' : OPENEULER2203,
         'DEST' : DOCUMENT_ROOT + '/pub/repositories/openEuler/22.03/bootstrap/'
     }
}
