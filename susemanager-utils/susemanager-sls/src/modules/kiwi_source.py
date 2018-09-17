import salt.exceptions
import logging
import os
from tempfile import mkdtemp
try:
    from urllib.parse import urlparse
except ImportError:
     from urlparse import urlparse

log = logging.getLogger(__name__)

# valid prefixes taken from Docker-CE to be compatible
valid_git_prefixes = ['http://', 'https://', 'git://', 'github.com/', 'git@']
valid_url_prefixes = ['http://', 'https://']
valid_url_suffixes = ['.tar.gz', '.tar.xz', '.tar.bz2', '.tgz', '.tar']

def _isLocal(source):
  return __salt__['file.directory_exists'](source)

def _isGit(source):
  for prefix in valid_git_prefixes:
    if source.startswith(prefix):
      return True
  return False

def _isTarball(source):
  prefix_ok = False
  for prefix in valid_url_prefixes:
    if source.startswith(prefix):
      prefix_ok = True
      break

  if not prefix_ok:
    return False

  for suffix in valid_url_suffixes:
    if source.endswith(suffix):
      return True

  return False

def _prepareDestDir(dest):
  '''
  Check target directory does not exists
  '''
  if os.path.isdir(dest):
    raise salt.exceptions.SaltException('Working directory "{}" exists before sources are prepared'.format(dest))

def _prepareLocal(source, dest):
  '''
  Make link from `source` to `dest`
  '''
  log.debug('Source is local directory')
  _prepareDestDir(dest)
  __salt__['file.symlink'](source, dest)
  return dest

def _prepareHTTP(source, dest):
  '''
  Download tarball and extract to the directory
  '''
  log.debug('Source is HTTP')
  _prepareDestDir(dest)

  filename = os.path.join(dest, source.split("/")[-1])
  res = __salt__['state.single']('file.managed', filename, source=source, makedirs=True, skip_verify=True)
  for s, r in list(res.items()):
    if not r['result']:
      raise salt.exceptions.SaltException(r['comment'])
  res = __salt__['state.single']('archive.extracted', name=dest, source=filename, skip_verify=True, overwrite=True)
  for s, r in list(res.items()):
    if not r['result']:
      raise salt.exceptions.SaltException(r['comment'])
  return dest

def _prepareGit(source, dest, root):
  _prepareDestDir(dest)

  # checkout git into temporary directory in our build root
  # this is needed if we are interested only in git subtree
  tmpdir = __salt__['temp.dir'](parent=root)

  rev = 'master'
  subdir = None
  url = None

  # parse git uri - i.e. git@github.com/repo/#rev:sub
  try:
    url, fragment = source.split('#', 1)
    try:
      rev, subdir = fragment.split(':', 1)
    except:
      rev = fragment
  except:
    url = source

  log.debug('GIT URL: {}, Revision: {}, subdir: {}'.format(url, rev, subdir))
  __salt__['git.init'](tmpdir)
  __salt__['git.remote_set'](tmpdir, url)
  __salt__['git.fetch'](tmpdir)
  __salt__['git.checkout'](tmpdir, rev=rev)

  if subdir and _isLocal(os.path.join(tmpdir, subdir)):
    __salt__['file.symlink'](os.path.join(tmpdir, subdir), dest)
  else:
    __salt__['file.symlink'](tmpdir, dest)
  return dest

def prepare_source(source, root):
  '''
  Prepare source directory based on different source types.

  source -- string with either local directory path, remote http(s) archive or git repository
  root   -- local directory where to store processed source files

  For git repository following format is understood:
    [http[s]://|git://][user@]hostname/repository[#revision[:subdirectory]]
  '''
  dest = os.path.join(root, 'source')
  log.debug('Preparing build source for {} to {}'.format(source, dest))
  if _isLocal(source):
    return _prepareLocal(source, dest)
  elif _isTarball(source):
    return _prepareHTTP(source, dest)
  elif _isGit(source):
    return _prepareGit(source, dest, root)
  else:
    raise salt.exceptions.SaltException('Unknown source format "{}"'.format(source))
