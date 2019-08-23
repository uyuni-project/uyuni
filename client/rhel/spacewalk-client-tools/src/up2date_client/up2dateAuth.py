#

import os
import pickle
import time

try: # python2
    from types import DictType
except ImportError: # python3
    DictType = dict

from rhn import rpclib
from up2date_client import clientCaps
from up2date_client import config
from up2date_client import rhnserver
from up2date_client import up2dateErrors
from up2date_client import up2dateLog
from up2date_client import up2dateUtils
from up2date_client.up2dateUtils import getMachineId

loginInfo = None
pcklAuthFileName = "/var/spool/up2date/loginAuth.pkl"
log = up2dateLog.initLog()

def getSystemId():
    log.log_debug("getSystemId invoked")
    cfg = config.initUp2dateConfig()
    path = cfg["systemIdPath"]
    ret = None
    if os.access(path, os.R_OK):
        try:
            # add machine_id on the fly
            cert = rpclib.xmlrpclib.loads(open(path, "r").read())
            machine_id = getMachineId()
            if machine_id:
                # do not append machine_id to fields to avoid breaking
                # the checksum and authentication.
                cert[0][0]["machine_id"] = machine_id
            ret = rpclib.xmlrpclib.dumps(cert[0])
        except Exception:
            log.log_me("ERROR - Unable to read XML in %s - %s" % (path, sys.exc_info()[1]))
    return ret

# if a user has upgraded to a newer release of Red Hat but still
# has a systemid from their older release, they need to get an updated
# systemid from the RHN servers.  This takes care of that.
def maybeUpdateVersion():
    cfg = config.initUp2dateConfig()
    try:
        idVer = rpclib.xmlrpclib.loads(getSystemId())[0][0]['os_release']
    except:
        # they may not even have a system id yet.
        return 0

    systemVer = up2dateUtils.getVersion()

    if idVer != systemVer:
      s = rhnserver.RhnServer()

      if s.capabilities.hasCapability('xmlrpc.packages.suse_products', 1):
          # do not change channels
          newSystemId = s.registration.upgrade_version(getSystemId(), systemVer, False)
      else:
          newSystemId = s.registration.upgrade_version(getSystemId(), systemVer)

      path = cfg["systemIdPath"]
      dir = path[:path.rfind("/")]
      if not os.access(dir, os.W_OK):
          try:
              os.mkdir(dir)
          except:
              return 0
      if not os.access(dir, os.W_OK):
          return 0

      statinfo = None
      if os.access(path, os.F_OK):
          # already have systemid file there; let's back it up
          savePath = path + ".save"
          try:
              statinfo = os.stat(path)
              os.rename(path, savePath)
          except:
              return 0

      f = open(path, "w")
      f.write(newSystemId)
      f.close()
      try:
          if statinfo:
              # update; keep permissions and ownership
              os.chmod(path, statinfo.st_mode)
              os.chown(path, statinfo.st_uid, statinfo.st_gid)
          else:
              # new file
              os.chmod(path, int('0600', 8))
      except:
          pass


def writeCachedLogin():
    """
    Pickle loginInfo to a file
    Returns:
    True    -- wrote loginInfo to a pickle file
    False   -- did _not_ write loginInfo to a pickle file
    """
    log.log_debug("writeCachedLogin() invoked")
    if not loginInfo:
        log.log_debug("writeCachedLogin() loginInfo is None, so bailing.")
        return False
    data = {'time': time.time(),
            'loginInfo': loginInfo}

    pcklDir = os.path.dirname(pcklAuthFileName)
    if not os.access(pcklDir, os.W_OK):
        try:
            os.mkdir(pcklDir)
            os.chmod(pcklDir, int('0700', 8))
        except:
            log.log_me("Unable to write pickled loginInfo to %s" % pcklDir)
            return False
    pcklAuth = open(pcklAuthFileName, 'wb')
    os.chmod(pcklAuthFileName, int('0600', 8))
    pickle.dump(data, pcklAuth)
    pcklAuth.close()
    expireTime = data['time'] + float(loginInfo['X-RHN-Auth-Expire-Offset'])
    log.log_debug("Wrote pickled loginInfo at ", data['time'], " with expiration of ",
            expireTime, " seconds.")
    return True

def readCachedLogin():
    """
    Read pickle info from a file
    Caches authorization info for connecting to the server.
    """
    log.log_debug("readCachedLogin invoked")
    if not os.access(pcklAuthFileName, os.R_OK):
        log.log_debug("Unable to read pickled loginInfo at: %s" % pcklAuthFileName)
        return False
    pcklAuth = open(pcklAuthFileName, 'rb')
    try:
        data = pickle.load(pcklAuth)
    except (EOFError, ValueError):
        log.log_debug("Unexpected EOF. Probably an empty file, \
                       regenerate auth file")
        pcklAuth.close()
        return False
    pcklAuth.close()
    # Check if system_id has changed
    try:
        idVer = rpclib.xmlrpclib.loads(getSystemId())[0][0]['system_id']
        cidVer = "ID-%s" % data['loginInfo']['X-RHN-Server-Id']
        if idVer != cidVer:
            log.log_debug("system id version changed: %s vs %s" % (idVer, cidVer))
            return False
    except:
        pass
    createdTime = data['time']
    li = data['loginInfo']
    currentTime = time.time()
    expireTime = createdTime + float(li['X-RHN-Auth-Expire-Offset'])
    #Check if expired, offset is stored in "X-RHN-Auth-Expire-Offset"
    log.log_debug("Checking pickled loginInfo, currentTime=", currentTime,
            ", createTime=", createdTime, ", expire-offset=",
            float(li['X-RHN-Auth-Expire-Offset']))

    # Refresh loginInfo cache 5 minutes (300 seconds) before its actual expiration
    if (currentTime + 300 > expireTime):
        log.log_debug("Pickled loginInfo will be expiring soon so refresh cache, created = %s, expire = %s." \
                %(createdTime, expireTime))
        return False
    _updateLoginInfo(li)
    log.log_debug("readCachedLogin(): using pickled loginInfo set to expire at ", expireTime)
    return True

def _updateLoginInfo(li):
    """
    Update the global var, "loginInfo"
    """
    global loginInfo
    if type(li) == DictType:
        if type(loginInfo) == DictType:
            # must retain the reference.
            loginInfo.update(li)
        else:
            # this had better be the initial login or we lose the reference.
            loginInfo = li
    else:
        loginInfo = None

# allow to pass in a system id for use in rhnreg
# a bit of a kluge to make caps work correctly
def login(systemId=None, forceUpdate=False, timeout=None):
    log.log_debug("login(forceUpdate=%s) invoked" % (forceUpdate))
    if not forceUpdate and not loginInfo:
        if readCachedLogin():
            return loginInfo

    server = rhnserver.RhnServer(timeout=timeout)

    # send up the capabality info
    headerlist = clientCaps.caps.headerFormat()
    for (headerName, value) in headerlist:
        server.add_header(headerName, value)

    if systemId == None:
        systemId = getSystemId()

    if not systemId:
        return None

    maybeUpdateVersion()
    log.log_me("logging into up2date server")

    li = server.up2date.login(systemId)

    # figure out if were missing any needed caps
    server.capabilities.validate()
    _updateLoginInfo(li) #update global var, loginInfo
    writeCachedLogin() #pickle global loginInfo

    if loginInfo:
        log.log_me("successfully retrieved authentication token "
                   "from up2date server")

    log.log_debug("logininfo:", loginInfo)
    return loginInfo

def updateLoginInfo(timeout=None):
    log.log_me("updateLoginInfo() login info")
    # NOTE: login() updates the loginInfo object
    login(forceUpdate=True, timeout=timeout)
    if not loginInfo:
        raise up2dateErrors.AuthenticationError("Unable to authenticate")
    return loginInfo


def getLoginInfo(timeout=None):
    global loginInfo
    try:
        loginInfo = loginInfo
    except NameError:
        loginInfo = None
    if loginInfo:
        return loginInfo
    # NOTE: login() updates the loginInfo object
    login(timeout=timeout)
    return loginInfo

