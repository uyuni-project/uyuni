'''
Author: Bo Maryniuk <bo@suse.de>
'''
import imp
import os


def load_module(path):
    '''
    Load module, relative to the package.
    '''
    return imp.load_source(
        os.path.basename(path).split(".")[0],
        os.path.sep.join(__file__.split(os.path.sep)[:-2] + path.split(os.path.sep)))
