import salt.exceptions  #  pylint: disable=missing-module-docstring
import logging
import os
from tempfile import mkdtemp  #  pylint: disable=unused-import

try:
    from urllib.parse import urlparse  #  pylint: disable=unused-import
except ImportError:
    from urlparse import urlparse

log = logging.getLogger(__name__)

# valid prefixes taken from Docker-CE to be compatible
valid_git_prefixes = ["http://", "https://", "git://", "github.com/", "git@"]
valid_url_prefixes = ["http://", "https://"]
valid_url_suffixes = [".tar.gz", ".tar.xz", ".tar.bz2", ".tgz", ".tar"]


def _isLocal(source):  #  pylint: disable=invalid-name
    return __salt__["file.directory_exists"](source)  #  pylint: disable=undefined-variable


def _isGit(source):  #  pylint: disable=invalid-name
    for prefix in valid_git_prefixes:
        if source.startswith(prefix):
            return True
    return False


def _isTarball(source):  #  pylint: disable=invalid-name
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


def _prepareDestDir(dest):  #  pylint: disable=invalid-name
    """
    Check target directory does not exists
    """
    if os.path.isdir(dest):
        raise salt.exceptions.SaltException(
            'Working directory "{0}" exists before sources are prepared'.format(dest)  #  pylint: disable=consider-using-f-string
        )


def _prepareLocal(source, dest):  #  pylint: disable=invalid-name
    """
    Make link from `source` to `dest`
    """
    log.debug("Source is local directory")
    _prepareDestDir(dest)
    __salt__["file.symlink"](source, dest)  #  pylint: disable=undefined-variable
    return dest


def _prepareHTTP(source, dest):  #  pylint: disable=invalid-name
    """
    Download tarball and extract to the directory
    """
    log.debug("Source is HTTP")
    _prepareDestDir(dest)

    filename = os.path.join(dest, source.split("/")[-1])
    res = __salt__["state.single"](  #  pylint: disable=undefined-variable
        "file.managed", filename, source=source, makedirs=True, skip_verify=True
    )
    for s, r in list(res.items()):  #  pylint: disable=unused-variable
        if not r["result"]:
            raise salt.exceptions.SaltException(r["comment"])
    res = __salt__["state.single"](  #  pylint: disable=undefined-variable
        "archive.extracted",
        name=dest,
        source=filename,
        skip_verify=True,
        overwrite=True,
    )
    for s, r in list(res.items()):
        if not r["result"]:
            raise salt.exceptions.SaltException(r["comment"])
    return dest


def _prepareGit(source, dest, root):  #  pylint: disable=invalid-name
    _prepareDestDir(dest)

    # checkout git into temporary directory in our build root
    # this is needed if we are interested only in git subtree
    tmpdir = __salt__["temp.dir"](parent=root)  #  pylint: disable=undefined-variable

    rev = "master"
    subdir = None
    url = None

    # parse git uri - i.e. git@github.com/repo/#rev:sub
    # compatible with docker as per https://docs.docker.com/engine/reference/commandline/build/#git-repositories  #  pylint: disable=line-too-long

    try:
        url, fragment = source.split("#", 1)
        try:
            rev, subdir = fragment.split(":", 1)
        except:  #  pylint: disable=bare-except
            rev = fragment
    except:  #  pylint: disable=bare-except
        url = source

    # omitted rev means default 'master' branch revision
    if rev == "":
        rev = "master"

    log.debug("GIT URL: {0}, Revision: {1}, subdir: {2}".format(url, rev, subdir))  #  pylint: disable=logging-format-interpolation,consider-using-f-string
    __salt__["git.init"](tmpdir)  #  pylint: disable=undefined-variable
    __salt__["git.remote_set"](tmpdir, url)  #  pylint: disable=undefined-variable
    __salt__["git.fetch"](tmpdir)  #  pylint: disable=undefined-variable
    __salt__["git.checkout"](tmpdir, rev=rev)  #  pylint: disable=undefined-variable

    if subdir:
        if _isLocal(os.path.join(tmpdir, subdir)):
            __salt__["file.symlink"](os.path.join(tmpdir, subdir), dest)  #  pylint: disable=undefined-variable
        else:
            raise salt.exceptions.SaltException(
                "Directory is not present in checked out source: {}".format(subdir)  #  pylint: disable=consider-using-f-string
            )
    else:
        __salt__["file.symlink"](tmpdir, dest)  #  pylint: disable=undefined-variable
    return dest


def prepare_source(source, root):
    """
    Prepare source directory based on different source types.

    source -- string with either local directory path, remote http(s) archive or git repository  #  pylint: disable=line-too-long
    root   -- local directory where to store processed source files

    For git repository following format is understood:
      [http[s]://|git://][user@]hostname/repository[#revision[:subdirectory]]
    """
    dest = os.path.join(root, "source")
    log.debug("Preparing build source for {0} to {1}".format(source, dest))  #  pylint: disable=logging-format-interpolation,consider-using-f-string
    if _isLocal(source):
        return _prepareLocal(source, dest)
    elif _isTarball(source):
        return _prepareHTTP(source, dest)
    elif _isGit(source):
        return _prepareGit(source, dest, root)
    else:
        raise salt.exceptions.SaltException(
            'Unknown source format "{0}"'.format(source)  #  pylint: disable=consider-using-f-string
        )
