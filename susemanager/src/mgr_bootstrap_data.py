#
# DO NOT EDIT !!!
#

PKGLIST10 = [
    "libaugeas0",
    "libnewt0_52",
    "libzypp",
    "newt",
    "openssl",
    "perl-WWW-Curl",
    "python-dmidecode",
    "python-ethtool",
    "python-newt",
    "python-openssl",
    "python-xml",
    "python2-rhnlib",
    "rpm-python",
    "satsolver-tools",
    "spacewalk-check",
    "spacewalk-client-setup",
    "spacewalk-client-tools",
    "mgr-daemon|spacewalksd",
    "suseRegister",
    "suseRegisterInfo",
    "yast2-ncurses",
    "yast2-perl-bindings",
    "yast2-pkg-bindings",
    "yast2-qt",
    "zypp-plugin-python",
    "zypp-plugin-spacewalk",
    "zypper"
]

PKGLIST11 = [
    "dbus-1-python",
    "libcurl4",
    "libnewt0_52",
    "libnl",
    "libopenssl0_9_8",
    "libsqlite3-0",
    "libxml2-python",
    "libzypp",
    "newt",
    "openssl",
    "python",
    "python-dmidecode",
    "python-ethtool",
    "python-newt",
    "python-openssl",
    "python-xml",
    "python2-rhnlib",
    "rpm-python",
    "satsolver-tools",
    "slang",
    "spacewalk-check",
    "python2-spacewalk-check",
    "spacewalk-client-setup",
    "python2-spacewalk-client-setup",
    "spacewalk-client-tools",
    "python2-spacewalk-client-tools",
    "python2-uyuni-common-libs",
    "mgr-daemon|spacewalksd",
    "suseRegisterInfo",
    "python2-suseRegisterInfo",
    "zypp-plugin-python",
    "zypp-plugin-spacewalk",
    "python2-zypp-plugin-spacewalk",
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
    "dbus-1-python",
    "hwdata",
    "libcurl4",
    "libgudev-1_0-0",
    "libnewt0_52",
    "libnl1",
    "libopenssl1_0_0",
    "libsqlite3-0",
    "libudev1",
    "udev",
    "openssl",
    "python-libxml2",
    "libzypp",
    "newt",
    "python",
    "python-cffi",
    "python-cryptography",
    "python-dmidecode",
    "python-ethtool",
    "python-gobject2",
    "python-gudev",
    "python2-hwdata",
    "python-newt",
    "python-pyasn1",
    "python-pycparser",
    "python-pyOpenSSL",
    "python-six",
    "python-xml",
    "python-pyudev",
    "python2-rhnlib",
    "rpm-python",
    "libsolv-tools",
    "libslang2",
    "spacewalk-check",
    "python2-spacewalk-check",
    "spacewalk-client-setup",
    "python2-spacewalk-client-setup",
    "spacewalk-client-tools",
    "python2-spacewalk-client-tools",
    "python2-uyuni-common-libs",
    "mgr-daemon|spacewalksd",
    "suseRegisterInfo",
    "python2-suseRegisterInfo",
    "zypp-plugin-python",
    "zypp-plugin-spacewalk",
    "python2-zypp-plugin-spacewalk",
    "zypper",
    "yast2-packager",
    "yast2-pkg-bindings",
    "python-backports.ssl_match_hostname",
    "python-futures",
    "python-Jinja2",
    "python-MarkupSafe",
    "python-msgpack-python",
    "python-psutil",
    "python-pycrypto",
    "python-PyYAML",
    "python-pyzmq",
    "python-requests",
    "python-simplejson",
    "python-singledispatch",
    "python-tornado",
    "salt",
    "salt-minion",
    "python2-salt",
    "libgio-2_0-0",
    "libgthread-2_0-0",
    "shared-mime-info",
    "glib2-tools",
    "libelf0",
    "logrotate",
    "cron",
    "cronie"
]

ONLYSLE12 = [
    "libzmq3",
    "gio-branding-SLE",
    "wallpaper-branding-SLE"
]

PKGLIST12_X86_ARM = [
    "dmidecode",
]

ONLYOPENSUSE42 = [
    "libzmq5",
    "gio-branding-openSUSE",
    "wallpaper-branding-openSUSE",
]

ENHANCE12 = [
    "libyui-ncurses-pkg6",
    "libyui-qt-pkg6",
    "PackageKit-backend-zypp",
    "PackageKit-lang",
]

ENHANCE12SP1 = [
    "libyui-ncurses-pkg7",
    "libyui-qt-pkg7",
    "python-asn1crypto",
    "python-enum34",
    "python-idna",
    "python-ipaddress",
    "python-packaging",
    "python-pyparsing",
    "python-setuptools",
]

# This is in fact only required for now for AWS SLE12SP5 x86_64 images,
# as they do not have dbus-1-glib installed
ENHANCE12SP5_X86 = [
    "dbus-1-glib",
    "girepository-1_0",
    "libcairo2",
    "libdatrie1",
    "libdrm2",
    "libdrm_amdgpu1",
    "libdrm_intel1",
    "libdrm_nouveau2",
    "libdrm_radeon1",
    "libgbm1",
    "libgirepository-1_0-1",
    "libgobject-2_0-0",
    "libgraphite2-3",
    "libharfbuzz0",
    "libLLVM7",
    "libpango-1_0-0",
    "libpciaccess0",
    "libpixman-1-0",
    "libthai0",
    "libthai-data",
    "libX11-xcb1",
    "libxcb-dri2-0",
    "libxcb-dri3-0",
    "libxcb-glx0",
    "libxcb-present0",
    "libxcb-render0",
    "libxcb-shm0",
    "libxcb-sync1",
    "libxcb-xfixes0",
    "libXdamage1",
    "libXfixes3",
    "libXft2",
    "libXrender1",
    "libxshmfence1",
    "libXxf86vm1",
    "Mesa",
    "Mesa-dri",
    "Mesa-libEGL1",
    "Mesa-libGL1",
    "Mesa-libglapi0",
    "python-gobject",
    "typelib-1_0-Pango-1_0",
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
    "python-backports-ssl_match_hostname",
    "python-backports",
    "python-certifi",
    "python-simplejson",
    "PyYAML",
    "python-markupsafe",
    "python-urllib3",
    "libyaml",
    "python-chardet",
    "python-six",
    "python-babel",
    "dbus",
    "dbus-libs",
    "yum-plugin-security",
    "yum-rhn-plugin",
    "yum",
    "python2-rhnlib",
    "rpm-python",
    "redhat-rpm-config",
    "slang",
    "spacewalk-check",
    "python2-spacewalk-check",
    "spacewalk-client-setup",
    "python2-spacewalk-client-setup",
    "spacewalk-client-tools",
    "python2-spacewalk-client-tools",
    "python2-uyuni-common-libs",
    "mgr-daemon|spacewalksd",
    "suseRegisterInfo",
    "python2-suseRegisterInfo",
    "python2-hwdata",
    "dmidecode",
    "openssh-clients",
    "libedit",
]

RES7 = [
    "salt",
    "salt-minion",
    "python2-salt",
    "python-futures",
    "python-jinja2",
    "python-msgpack-python",
    "python-psutil",
    "python-pycrypto",
    "python-requests",
    "python-setuptools",
    "python-singledispatch",
    "python-tornado",
    "python-zmq",
    "zeromq",
    "python-backports-ssl_match_hostname",
    "python-backports",
    "python-certifi",
    "python-simplejson",
    "PyYAML",
    "python-markupsafe",
    "python-urllib3",
    "libyaml",
    "python-chardet",
    "python-six",
    "python-babel",
    "yum-rhn-plugin",
    "yum",
    "python2-rhnlib",
    "openssl",
    "openssl-libs",
    "python-ipaddress",
    "redhat-rpm-config",
    "rpm-python",
    "spacewalk-check",
    "python2-spacewalk-check",
    "spacewalk-client-setup",
    "python2-spacewalk-client-setup",
    "spacewalk-client-tools",
    "python2-spacewalk-client-tools",
    "python2-uyuni-common-libs",
    "mgr-daemon|spacewalksd",
    "suseRegisterInfo",
    "python2-suseRegisterInfo",
    "python2-hwdata",
    "dmidecode",
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
    "dmidecode",
    "python3-urllib3",
    "python3-idna",
    "python3-chardet",
    "python3-pysocks",
    "python3-pytz",
    "python3-setuptools",
    "python3-distro"
]

PKGLIST15_SALT = [
    "libpgm-5_2-0",
    "libsodium23",
    "libzmq5",
    "python3-Babel",
    "python3-certifi",
    "python3-chardet",
    "python3-distro",
    "python3-idna",
    "python3-Jinja2",
    "python3-MarkupSafe",
    "python3-M2Crypto",
    "python3-msgpack",
    "python3-psutil",
    "python3-py",
    "python3-pytz",
    "python3-PyYAML",
    "python3-pyzmq",
    "python3-requests",
    "python3-rpm",
    "python3-simplejson",
    "python3-six",
    "python3-urllib3",
    "timezone",
    "salt",
    "python3-salt",
    "salt-minion",
]

PKGLIST15SP0SP1_SALT = [
    "python3-tornado",
]

ONLYSLE15 = [
    "gio-branding-SLE",
]

PKGLIST15_TRAD = [
    "dbus-1-glib",
    "glib2-tools",
    "girepository-1_0",
    "libgudev-1_0-0",
    "libgirepository-1_0-1",
    "libgio-2_0-0",
    "libgobject-2_0-0",
    "libnewt0_52",
    "libslang2",
    "newt",
    "python3-asn1crypto",
    "python3-cffi",
    "python3-cryptography",
    "python-dmidecode",
    "python3-dbus-python",
    "python3-dmidecode",
    "python3-gobject",
    "python3-libxml2-python",
    "python3-netifaces",
    "python3-newt",
    "python3-pyasn1",
    "python3-pycparser",
    "python3-pyOpenSSL",
    "python3-pyudev",
    "python3-packaging",
    "python3-setuptools",
    "python3-appdirs",
    "python3-pyparsing",
    "hwdata",
    "python3-hwdata",
    "python3-rhnlib",
    "spacewalk-check",
    "spacewalk-client-setup",
    "spacewalk-client-tools",
    "python3-spacewalk-check",
    "python3-spacewalk-client-setup",
    "python3-spacewalk-client-tools",
    "python3-uyuni-common-libs*",
    "mgr-daemon|spacewalksd",
    "shared-mime-info",
    "suseRegisterInfo",
    "python3-suseRegisterInfo",
    "zypp-plugin-spacewalk",
    "python3-zypp-plugin",
    "python3-zypp-plugin-spacewalk",
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
    "salt-common",
    "salt-minion",
]

DATA = {
    'SLE-11-SP1-i586' : {
        'PDID' : 684, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-ia64' : {
        'PDID' : 1033, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-ppc64' : {
        'PDID' : 940, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-s390x' : {
        'PDID' : 745, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP1-x86_64' : {
        'PDID' : 769, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLE-11-SP2-i586' : {
        'PDID' : 811, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-ia64' : {
        'PDID' : 1034, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-ppc64' : {
        'PDID' : 946, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-s390x' : {
        'PDID' : 755, 'PKGLIST' : PKGLIST11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP2-x86_64' : {
        'PDID' : 690, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLE-11-SP3-i586' : {
        'PDID' : 793, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-ia64' : {
        'PDID' : 1037, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-ppc64' : {
        'PDID' : 949, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-s390x' : {
        'PDID' : 693, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP3-x86_64' : {
        'PDID' : 814, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLE-11-SP4-i586' : {
        'PDID' : 1299, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-ia64' : {
        'PDID' : 1302, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-ppc64' : {
        'PDID' : 1301, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-s390x' : {
        'PDID' : 1303, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-11-SP4-x86_64' : {
        'PDID' : 1300, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-10-SP3-i586' : {
        'PDID' : 785, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
    },
    'SLE-10-SP3-ia64' : {
        'PDID' : 740, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
    },
    'SLE-10-SP3-ppc' : {
        'PDID' : 787, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
    },
    'SLE-10-SP3-s390x' : {
        'PDID' : 682, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
    },
    'SLE-10-SP3-x86_64' : {
        'PDID' : 721, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
    },
    'SLE-10-SP4-i586' : {
        'PDID' : 752, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
    },
    'SLE-10-SP4-ia64' : {
        'PDID' : 770, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
    },
    'SLE-10-SP4-ppc' : {
        'PDID' : 711, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
    },
    'SLE-10-SP4-s390x' : {
        'PDID' : 771, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
    },
    'SLE-10-SP4-x86_64' : {
        'PDID' : 832, 'PKGLIST' : PKGLIST10,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
    },
    'SLES4SAP-11-SP1-x86_64' : {
        'PDID' : 1129, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
    },
    'SLES4SAP-11-SP2-x86_64' : {
        'PDID' : 1130, 'PKGLIST' : PKGLIST11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
    },
    'SLES4SAP-11-SP3-x86_64' : {
        'PDID' : 1131, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/3/bootstrap/'
    },
    'SLES4SAP-11-SP4-x86_64' : {
        'PDID' : 1329, 'PKGLIST' : PKGLIST11 + ENHANCE11 + PKGLIST11_X86_I586,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLES4SAP-11-SP4-ppc64' : {
        'PDID' : 1331, 'PKGLIST' : PKGLIST11 + ENHANCE11,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/4/bootstrap/'
    },
    'SLE-12-ppc64le' : {
        'PDID' : 1116, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLE-12-s390x' : {
        'PDID' : 1115, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLE-12-x86_64' : {
        'PDID' : 1117, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/0/bootstrap/'
    },
    'SLES4SAP-12-x86_64' : {
        'PDID' : 1319, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/0/1/bootstrap/'
    },
    'SLE-12-SP1-ppc64le' : {
        'PDID' : 1334, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLE-12-SP1-s390x' : {
        'PDID' : [1335, 1535], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLE-12-SP1-x86_64' : {
        'PDID' : [1322, 1533], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/1/bootstrap/'
    },
    'SLES4SAP-12-SP1-ppc64le' : {
        'PDID' : 1437, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/1/0/1/bootstrap/'
    },
    'SLES4SAP-12-SP1-x86_64' : {
        'PDID' : 1346, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/1/0/1/bootstrap/'
    },
    'RES6-x86_64' : {
        'PDID' : 1138, 'PKGLIST' : RES6,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/6/bootstrap/'
    },
    'RES7-x86_64' : {
        'PDID' : 1251, 'PKGLIST' : RES7,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/7/bootstrap/'
    },
    'SLE-12-SP2-aarch64' : {
        'PDID' : 1375, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES_RPI-12-SP2-aarch64' : {
        'PDID' : 1418, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-ppc64le' : {
        'PDID' : [1355, 1737], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-s390x' : {
        'PDID' : [1356, 1738], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP2-x86_64' : {
        'PDID' : [1357, 1739], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES4SAP-12-SP2-x86_64' : {
        'PDID' : 1414, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLES4SAP-12-SP2-ppc64le' : {
        'PDID' : 1521, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'SLE-12-SP3-aarch64' : {
        'PDID' : [1424, 2002], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-ppc64le' : {
        'PDID' : [1422, 1930], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-s390x' : {
        'PDID' : [1423, 1931], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP3-x86_64' : {
        'PDID' : [1421, 1932], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLES4SAP-12-SP3-x86_64' : {
        'PDID' : 1426, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLES4SAP-12-SP3-ppc64le' : {
        'PDID' : 1572, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'SLE-12-SP4-aarch64' : {
        'PDID' : [1628, 2114], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-ppc64le' : {
        'PDID' : [1626, 2115], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-s390x' : {
        'PDID' : [1627, 2116], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP4-x86_64' : {
        'PDID' : [1625, 2117], 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLED-12-SP4-x86_64' : {
        'PDID' : 1629, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLES4SAP-12-SP4-x86_64' : {
        'PDID' : 1755, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLES4SAP-12-SP4-ppc64le' : {
        'PDID' : 1754, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE4HPC-12-SP4-x86_64' : {
        'PDID' : 1759, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE4HPC-12-SP4-aarch64' : {
        'PDID' : 1758, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/4/bootstrap/'
    },
    'SLE-12-SP5-aarch64' : {
        'PDID' : 1875, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-ppc64le' : {
        'PDID' : 1876, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-s390x' : {
        'PDID' : 1877, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-12-SP5-x86_64' : {
        'PDID' : 1878, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + ENHANCE12SP5_X86 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLES4SAP-12-SP5-x86_64' : {
        'PDID' : 1880, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + ENHANCE12SP5_X86 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLES4SAP-12-SP5-ppc64le' : {
        'PDID' : 1879, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE4HPC-12-SP5-x86_64' : {
        'PDID' : 1873, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + ENHANCE12SP5_X86 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE4HPC-12-SP5-aarch64' : {
        'PDID' : 1872, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'OES2018-x86_64' : {
        'PDID' : 45, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/2/bootstrap/'
    },
    'OES2018-SP1-x86_64' : {
        'PDID' : 46, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/3/bootstrap/'
    },
    'OES2018-SP2-x86_64' : {
        'PDID' : -9, 'PKGLIST' : PKGLIST12 + ONLYSLE12 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/12/5/bootstrap/'
    },
    'SLE-15-aarch64' : {
        'PDID' : [1589, 2053, 1709], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-ppc64le' : {
        'PDID' : [1588, 2054, 1710], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_PPC,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-s390x' : {
        'PDID' : [1587, 2055, 1711], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_Z,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-x86_64' : {
        'PDID' : [1576, 2056, 1712], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/0/bootstrap/'
    },
    'SLE-15-SP1-aarch64' : {
        'PDID' : [1769, 1709], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-ppc64le' : {
        'PDID' : [1770, 1710], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_PPC,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-s390x' : {
        'PDID' : [1771, 1711], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_Z,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP1-x86_64' : {
        'PDID' : [1772, 1712], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/1/bootstrap/'
    },
    'SUMA-40-PROXY-x86_64' : {
        'PDID' : [1772, 1908], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/1/bootstrap/'
    },
    'SLE-15-SP2-aarch64' : {
        'PDID' : [1943, 1709], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-ppc64le' : {
        'PDID' : [1944, 1710], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-s390x' : {
        'PDID' : [1945, 1711], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP2-x86_64' : {
        'PDID' : [1946, 1712], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/2/bootstrap/'
    },
    'SLE-15-SP3-aarch64' : {
        'PDID' : [2142, 1709], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-ppc64le' : {
        'PDID' : [2143, 1710], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_PPC,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-s390x' : {
        'PDID' : [2144, 1711], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_Z,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/3/bootstrap/'
    },
    'SLE-15-SP3-x86_64' : {
        'PDID' : [2145, 1712], 'PKGLIST' : PKGLIST15_TRAD + ONLYSLE15 + PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/sle/15/3/bootstrap/'
    },
    'openSUSE-Leap-42.3-x86_64' : {
        'BASECHANNEL' : 'opensuse_leap42_3-x86_64', 'PKGLIST' : PKGLIST12 + ONLYOPENSUSE42 + ENHANCE12SP1 + PKGLIST12_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/opensuse/42/3/bootstrap/'
    },
    'openSUSE-Leap-15-x86_64' : {
        'BASECHANNEL' : 'opensuse_leap15_0-x86_64', 'PKGLIST' : PKGLIST15_TRAD + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/opensuse/15/0/bootstrap/'
    },
    'openSUSE-Leap-15.1-x86_64-uyuni' : {
        'BASECHANNEL' : 'opensuse_leap15_1-x86_64', 'PKGLIST' : PKGLIST15_TRAD + PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/opensuse/15/1/bootstrap/'
    },
    'openSUSE-Leap-15.1-x86_64' : {
        'PDID' : [1929], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15SP0SP1_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/opensuse/15/1/bootstrap/'
    },
    'openSUSE-Leap-15.2-x86_64' : {
        'PDID' : [2001], 'PKGLIST' : PKGLIST15_SALT + PKGLIST15_X86_ARM,
        'DEST' : '/srv/www/htdocs/pub/repositories/opensuse/15/2/bootstrap/'
    },
    'centos-6-x86_64' : {
        'BASECHANNEL' : 'centos6-x86_64', 'PKGLIST' : RES6,
        'DEST' : '/srv/www/htdocs/pub/repositories/centos/6/bootstrap/'
    },
    'centos-7-x86_64' : {
        'BASECHANNEL' : 'centos7-x86_64', 'PKGLIST' : RES7,
        'DEST' : '/srv/www/htdocs/pub/repositories/centos/7/bootstrap/'
    },
    'centos-8-x86_64' : {
        'BASECHANNEL' : 'centos8-x86_64', 'PKGLIST' : RES8,
        'DEST' : '/srv/www/htdocs/pub/repositories/centos/8/bootstrap/'
    },
    'RHEL6-x86_64' : {
        'PDID' : [-5, 1682], 'PKGLIST' : RES6,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/6/bootstrap/'
    },
    'RHEL6-i386' : {
        'PDID' : [-6, 1681], 'PKGLIST' : RES6,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/6/bootstrap/'
    },
    'RHEL7-x86_64' : {
        'PDID' : [-7, 1683], 'PKGLIST' : RES7,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/7/bootstrap/'
    },
    'SLE-ES8-x86_64' : {
        'PDID' : [-8, 1921, 2007], 'PKGLIST' : RES8,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/8/bootstrap/'
    },
    'RHEL8-x86_64' : {
        'PDID' : [-8, 2007], 'PKGLIST' : RES8,
        'DEST' : '/srv/www/htdocs/pub/repositories/res/8/bootstrap/'
    },
    'ubuntu-16.04-amd64' : {
        'PDID' : [-2, 1917], 'PKGLIST' : PKGLISTUBUNTU1604,
        'DEST' : '/srv/www/htdocs/pub/repositories/ubuntu/16/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-18.04-amd64' : {
        'PDID' : [-1, 1918], 'PKGLIST' : PKGLISTUBUNTU1804,
        'DEST' : '/srv/www/htdocs/pub/repositories/ubuntu/18/4/bootstrap/',
        'TYPE' : 'deb'
    },
    'ubuntu-20.04-amd64' : {
        'PDID' : [-18, 2113], 'PKGLIST' : PKGLISTUBUNTU2004,
        'DEST' : '/srv/www/htdocs/pub/repositories/ubuntu/20/4/bootstrap/',
        'TYPE' : 'deb'
    }
}
