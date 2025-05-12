#!/usr/bin/python
# pylint: disable=missing-module-docstring,invalid-name

import sys
import rpm

sys.path.append("../")

# pylint: disable-next=wrong-import-position,unused-import
import settestpath

# pylint: disable-next=wrong-import-position
from up2date_client import transaction

# lots of useful util methods for building/tearing down
# test enviroments...
# pylint: disable-next=wrong-import-position
import testutils

# pylint: disable-next=wrong-import-position
import unittest

# pylint: disable-next=wrong-import-position,ungrouped-imports,unused-import
from up2date_client import up2dateAuth


def write(blip):
    # pylint: disable-next=consider-using-f-string
    sys.stdout.write("\n|%s|\n" % blip)


# pylint: disable-next=missing-class-docstring
class TestGetAllPkgInfo(unittest.TestCase):
    # pylint: disable-next=invalid-name
    def __allPkgs(self, count, multilib=None, arch1=None, arch2=None):
        # pylint: disable-next=invalid-name
        self.allPkgs = []
        if not arch1:
            arch1 = "noarch"
        for i in range(count):
            if multilib:
                # pylint: disable-next=consider-using-f-string
                self.allPkgs.append(["a%04d" % i, "1.0", "1", "", arch1])
                # pylint: disable-next=consider-using-f-string
                self.allPkgs.append(["a%04d" % i, "1.0", "1", "", arch2])
            else:
                # pylint: disable-next=consider-using-f-string
                self.allPkgs.append(["a%04d" % i, "1.0", "1", "", arch1])

    def setUp(self):
        self.__allPkgs(5, multilib=1, arch1="i386", arch2="x86_64")
        # pylint: disable-next=invalid-name
        self.correctChoice = ["a0001", "1.0", "1", "", "x86_64"]


#    def testGetAllPkgInfoNoArch(self):
#        res = up2date.getAllPkgInfo(self.allPkgs, ['a0001', '1.0', '1', '', ''])
#        write("res:  %s" % res)
#        self.assertEqual(self.correctChoice, res)


# pylint: disable-next=missing-class-docstring
class TestTransactionData(unittest.TestCase):
    def setUp(self):
        self.__testData()
        self.td = transaction.TransactionData()

    # pylint: disable-next=invalid-name
    def __testData(self):
        self.foo = 1
        self.packages1 = [
            (("kernel", "2.4.0", "1.0", ""), "u"),
            (("blippy", "1.0", "1.0", "", "i686"), "i"),
        ]

    def testPopulateData1(self):
        "Verify that populating data set 1 works correctly"
        self.td.data["packages"] = self.packages1

    def testPrintData1(self):
        "Verify that the string repr of the transaction of data1 is correct"
        self.td.data["packages"] = self.packages1
        res = self.td.display()
        expected = """\t\t[i] blippy-1.0-1.0:\n\t\t[u] kernel-2.4.0-1.0:\n"""
        self.assertEqual(expected, res)


class TestGenTransaction(unittest.TestCase):
    def setUp(self):
        self.__setupData()

    # pylint: disable-next=invalid-name
    def __setupData(self):
        self.packages1 = [
            (("kernel", "2.4.0", "1.0", ""), "u"),
            (("blippy", "1.0", "1.0", "", "i686"), "i"),
        ]


#    def testGenTransactionData1(self):
#        td = transaction.TransactionData()
#        td.data['packages'] = self.packages1

#        res = up2date.genTransaction(td)
#        write(res)
#        print res


# pylint: disable-next=missing-class-docstring
class TestGenTransactionSat1(unittest.TestCase):
    setupflag = 0

    # pylint: disable-next=invalid-name
    def _realSetup(self, testname):
        rpm.addMacro("_dbpath", testutils.DBPATH)
        repackagedir = "/tmp/testrepackage"
        rpm.addMacro("_repackage_dir", repackagedir)

        if TestGenTransactionSat1.setupflag != 0:
            return

        testutils.createDataDirs()
        testutils.rebuildRpmDatabase(testname)
        testutils.rebuildRepackageDir(testname)
        TestGenTransactionSat1.setupflag = 1

    def setUp(self):
        self.__setupData()
        self._realSetup("8.0-workstation-i386-1")

    #        testutils.setupConfig("8.0-workstation-i386-1")

    # pylint: disable-next=invalid-name
    def __setupData(self):
        self.packages1 = [
            (("wget", "1.8.2", "5", ""), "u"),
            (("cvs", "1.11.2", "8", ""), "u"),
        ]

    #        self.packages1 = [(('automake', '1.6.3', '1', ''), 'u'),
    #                          (('sylpheed', '0.8.2', '1', ''), "u")]
    #        self.packages1 = [(('kernel', '2.4.0', '1.0', ''), "u"),
    #                          (('blippy', '1.0', '1.0', '', 'i686'), "i")]#

    def tearDown(self):
        pass


#       testutils.restoreConfig()


#    def testGenTransactionData1(self):
#        td = transaction.TransactionData()
#        td.data['packages'] = self.packages1

#        res = up2date.genTransaction(td)
#        write(res)
#       print res

#    def testRunTransactionData1(self):
#        td = transaction.TransactionData()
#        td.data['packages'] = self.packages1

#        res = up2date.genTransaction(td)

# for rpmCallback...
#        import wrapperUtils
#        rpmCallback = wrapperUtils.RpmCallback()
#        foo = up2date.runTransaction(res, rpmCallback.callback)
#        write(foo)


def suite():
    # pylint: disable-next=redefined-outer-name
    suite = unittest.TestSuite()
    suite.addTest(unittest.makeSuite(TestGenTransactionSat1))
    suite.addTest(unittest.makeSuite(TestGenTransaction))
    suite.addTest(unittest.makeSuite(TestTransactionData))
    suite.addTest(unittest.makeSuite(TestGetAllPkgInfo))
    return suite


if __name__ == "__main__":
    unittest.main(defaultTest="suite")
