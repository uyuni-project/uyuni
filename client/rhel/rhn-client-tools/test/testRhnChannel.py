#!/usr/bin/python

import sys

import settestpath

from up2date_client import rhnChannel
# lots of useful util methods for building/tearing down
# test enviroments...
import testutils

import unittest


def write(blip):
    sys.stdout.write("\n|%s|\n" % blip)


class testRhnChannel(unittest.TestCase):
    def setUp(self):
        self.__setupData()
        testutils.setupConfig("channelTest1")

    def tearDown(self):
        testutils.restoreConfig()

    def __setupData(self):
        self.channelListDetails = ['gpg_key_url', 'url', 'arch', 
             'description', 'org_id', 'label', 'version', 'local_channel', 
             'parent_channel', 'summary', 'type', 'id', 'name']

    def testGetChannelDetails(self):
        "rhnChannel.GetChannelDetails"
        res = rhnChannel.getChannelDetails()
        # since most of the values are host-specific, we'll just test
        # that we have the right keys
        self.assertEqual(sorted(res[0].dict.keys()),
                         sorted(self.channelListDetails))

    def testGetChannels(self):
        "Test rhnChannel.getChannels()"
        res = rhnChannel.getChannels()

        write(res)

def suite():
    suite = unittest.TestSuite()
    suite.addTest(unittest.makeSuite(testRhnChannel))
    return suite

if __name__ == "__main__":
    unittest.main(defaultTest="suite")
