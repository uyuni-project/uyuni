#
# DO NOT EDIT !!!
#

PKGLIST10 = ["spacewalk-client-tools", "zypper", "libzypp", "satsolver-tools",
             "zypp-plugin-python", "zypp-plugin-spacewalk", "spacewalk-check",
             "spacewalk-client-setup", "newt", "libnewt0_52", "python-newt",
             "python-dmidecode", "python-ethtool", "python-openssl", "rhnlib",
             "spacewalksd", "suseRegisterInfo", "yast2-ncurses", "yast2-qt",
             "yast2-perl-bindings", "yast2-pkg-bindings", "suseRegister",
             "libaugeas0", "perl-WWW-Curl", "python-xml", "rpm-python",
             "suseRegister"]

PKGLIST11 = ["spacewalk-client-tools", "zypper", "libzypp", "satsolver-tools",
            "zypp-plugin-python", "zypp-plugin-spacewalk", "spacewalk-check",
            "spacewalk-client-setup", "newt", "libnewt0_52", "python-newt",
            "python-dmidecode", "python-ethtool", "python-openssl", "rhnlib",
            "spacewalksd", "suseRegisterInfo", "libcurl4"] 

DATA = {
    'SLE-11-SP1-i586' : {
                          'PDID' : 1826, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
                        },
    'SLE-11-SP1-ia64' : {
                          'PDID' : 1832, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
                        },
    'SLE-11-SP1-ppc64' : {
                          'PDID' : 1831, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
                        },
    'SLE-11-SP1-s390x' : {
                          'PDID' : 1834, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
                        },
    'SLE-11-SP1-x86_64' : {
                          'PDID' : 1829, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/1/bootstrap/'
                        },
    'SLE-11-SP2-i586' : {
                          'PDID' : 3006, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
                        },
    'SLE-11-SP2-ia64' : {
                          'PDID' : 3012, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
                        },
    'SLE-11-SP2-ppc64' : {
                          'PDID' : 3011, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
                        },
    'SLE-11-SP2-s390x' : {
                          'PDID' : 3014, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
                        },
    'SLE-11-SP2-x86_64' : {
                          'PDID' : 3009, 'PKGLIST' : PKGLIST11,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/11/2/bootstrap/'
                        },
    'SLE-10-SP3-i586' : {
                          'PDID' : 1501, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
                        },
    'SLE-10-SP3-ia64' : {
                          'PDID' : 1507, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
                        },
    'SLE-10-SP3-ppc' : {
                          'PDID' : 1505, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
                        },
    'SLE-10-SP3-s390x' : {
                          'PDID' : 1509, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
                        },
    'SLE-10-SP3-x86_64' : {
                          'PDID' : 1504, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/3/bootstrap/'
                        },
    'SLE-10-SP4-i586' : {
                          'PDID' : 2523, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
                        },
    'SLE-10-SP4-ia64' : {
                          'PDID' : 2529, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
                        },
    'SLE-10-SP4-ppc' : {
                          'PDID' : 2527, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
                        },
    'SLE-10-SP4-s390x' : {
                          'PDID' : 2531, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
                        },
    'SLE-10-SP4-x86_64' : {
                          'PDID' : 2526, 'PKGLIST' : PKGLIST10,
                          'DEST' : '/srv/www/htdocs/pub/repositories/sle/10/4/bootstrap/'
                        },
}


