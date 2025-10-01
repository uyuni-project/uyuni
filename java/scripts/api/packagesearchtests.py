#!/usr/bin/python
# pylint: disable=missing-module-docstring

import unittest

# pylint: disable-next=wildcard-import
from config import *


# pylint: disable-next=missing-class-docstring,undefined-variable
class PackageSearchTests(RhnTestCase):

    # pylint: disable-next=invalid-name
    def test_searchByNameAndSummary(self):
        """
        Search for a package by name or summary
        """
        query = "apache"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.nameAndSummary(self.session_key, query)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))

    # pylint: disable-next=invalid-name
    def test_searchByNameAndDescription(self):
        """
        Search for a package by name or description
        """
        query = "virt"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.nameAndDescription(self.session_key, query)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))

    # pylint: disable-next=invalid-name
    def test_searchByName(self):
        """
        Search for a package by name
        """
        query = "vim"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.name(self.session_key, query)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))

    # pylint: disable-next=invalid-name
    def test_searchFreeFormSpecificVersion(self):
        """
        Search for a subset of available kernel packages
        """
        # pylint: disable-next=invalid-name
        luceneQuery = "(name:kernel AND -name:devel) AND version:2.6.18 AND (release:53.el5 OR release:92.el5)"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.advanced(self.session_key, luceneQuery)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))

    # pylint: disable-next=invalid-name
    def test_searchFreeFormWithChannel(self):
        """
        Search for virt packages in a particular channel
        """
        # pylint: disable-next=invalid-name
        luceneQuery = "name:virt"
        # pylint: disable-next=invalid-name
        channelLabel = "rhel-i386-server-vt-5"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.advancedWithChannel(
            self.session_key, luceneQuery, channelLabel
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))

    # pylint: disable-next=invalid-name
    def test_searchFreeFormWithActKey(self):
        """
        Search for virt packages in a particular channel
        """
        # pylint: disable-next=invalid-name
        luceneQuery = "name:vim OR name:sh"
        actkey = "1-testkeyname"
        # pylint: disable-next=undefined-variable
        pkgs = client.packages.search.advancedWithActKey(
            self.session_key, luceneQuery, actkey
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(pkgs != None)
        self.assertTrue(len(pkgs) > 0)
        for p in pkgs:
            # print "Package name = %s, version = %s, release = %s" % (p["name"], p["version"], p["release"])
            self.assertTrue(p.has_key("id"))
            self.assertTrue(p.has_key("name"))
            self.assertTrue(p.has_key("epoch"))
            self.assertTrue(p.has_key("version"))
            self.assertTrue(p.has_key("release"))
            self.assertTrue(p.has_key("arch"))
            self.assertTrue(p.has_key("description"))
            self.assertTrue(p.has_key("summary"))
        # print "%s packages were returned" % (len(pkgs))


if __name__ == "__main__":
    unittest.main()
