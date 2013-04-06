
# all the crap that is stored on the rhn side of stuff
# updating/fetching package lists, channels, etc


import os
import tempfile
import ConfigParser
import up2dateAuth
import up2dateLog
import rhnserver
import pkgUtils

from suseRegister.info import getProductProfile, parseProductProfileFile


def logDeltaPackages(pkgs):
    log = up2dateLog.initLog()
    log.log_me("Adding packages to package profile: %s" %
               pprint_pkglist(pkgs['added']))
    log.log_me("Removing packages from package profile: %s" %
               pprint_pkglist(pkgs['removed']))

def updatePackageProfile(timeout=None):
    """ get a list of installed packages and send it to rhnServer """
    log = up2dateLog.initLog()
    log.log_me("Updating package profile")
    packages = pkgUtils.getInstalledPackageList(getArch=1)
    s = rhnserver.RhnServer(timeout=timeout)
    if not s.capabilities.hasCapability('xmlrpc.packages.extended_profile', 2):
        # for older satellites and hosted - convert to old format
        packages = convertPackagesFromHashToList(packages)
    s.registration.update_packages(up2dateAuth.getSystemId(), packages)

    if s.capabilities.hasCapability('xmlrpc.packages.suse_products', 1):
	# also send information about the installed products
	log.log_me('Updating product profile')
	productProfile = getProductProfile()
	s.registration.suse_update_products(up2dateAuth.getSystemId(),
					    productProfile['guid'],
					    productProfile['secret'],
					    productProfile['ostarget'],
					    productProfile['products'])

def pprint_pkglist(pkglist):
    if type(pkglist) == type([]):
        output = map(lambda a : "%s-%s-%s" % (a[0],a[1],a[2]), pkglist)
    else:
        output = "%s-%s-%s" % (pkglist[0], pkglist[1], pkglist[2])
    return output

def convertPackagesFromHashToList(packages):
    """ takes list of hashes and covert it to list of lists
        resulting strucure is:
        [[name, version, release, epoch, arch, cookie], ... ]
    """
    result = []
    for package in packages:
        if package.has_key('arch') and package.has_key('cookie'):
            result.append([package['name'], package['version'], package['release'],
                package['epoch'], package['arch'], package['cookie']])
        elif package.has_key('arch'):
            result.append([package['name'], package['version'], package['release'],
                package['epoch'], package['arch']])
        else:
            result.append([package['name'], package['version'], package['release'], package['epoch']])
    return result

def customUpdateProductProfile(productProfile):
    """ Send a specific product profile to the server (mostly for testing) """
    log = up2dateLog.initLog()
    s = rhnserver.RhnServer()
    if s.capabilities.hasCapability('xmlrpc.packages.suse_products', 1):
	log.log_me('Updating product profile')
	s.registration.suse_update_products(up2dateAuth.getSystemId(),
					    productProfile['guid'],
					    productProfile['secret'],
					    productProfile['ostarget'],
					    productProfile['products'])
