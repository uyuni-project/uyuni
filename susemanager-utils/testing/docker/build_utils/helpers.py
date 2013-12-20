import os
import re
import shutil
import sys
import tempfile
from distutils.version import StrictVersion
from paver.easy import *

from constants import *

@task
def supported_branches():
  """List all the git branches supported by our containers"""
  print 'Allowed branches are: '
  for branch in GIT_BRANCH_BASE_CONTAINER.keys():
    print '  - ', branch

@task
def supported_dbs():
  """List all the dbs supported by our containers"""
  print 'Allowed dbs are: '
  for db in KNOWN_DBS:
    print '  - ', db

@task
def supported_targets():
  """List all the targets supported by our containers"""
  print 'Allowed targets are: '
  for test_target in TEST_TARGETS:
    print '  - ', test_target

def check_kiwi():
  """Ensure the right version of kiwi is installed."""
  if not os.path.exists('/usr/sbin/kiwi'):
    error('kiwi binary not found, please install kiwi from obs://virtualization:appliances')
    sys.exit(1)

  kiwi_version = sh('rpm -q --qf %{VERSION} kiwi', capture=True)

  if not StrictVersion(kiwi_version) >= StrictVersion('5.5.26'):
    error('You have an outdated version of kiwi, please install kiwi >= 5.5.26')
    sys.exit(1)

def container_exists(container_name):
  """Return True if the container exists, False otherwise."""

  container_id = sh('docker images -q=true {0}'.format(container_name), capture=True)
  return len(container_id) != 0

def build_container_using_kiwi(kiwi_files_dir, container_name, build_only):
  """Builds the container using kiwi."""

  check_kiwi()

  build_dir = tempfile.mkdtemp(prefix = 'kiwi_build_{0}_'.format(container_name))

  with pushd(build_dir):
    print "Creating the lxc container using kiwi"
    sh("sudo /usr/sbin/kiwi --prepare {0} --root {1}_rootfs --force-new-root".format(kiwi_files_dir, container_name))
    print "Compressing the lxc container root"
    sh("sudo tar cpf {0}.tar -C {0}_rootfs .".format(container_name))

    if build_only:
      shutil.move(container_name + '.tar', kiwi_files_dir)
      print 'The container tar has been moved to your working directory'
    else:
      # import the container
      print "Importing the container locally"
      sh('docker import - {0} < {0}.tar'.format(container_name))
      print "{0} container has been successfully built and imported.".format(container_name)

  # remove the temp directory, ignore errors because sometimes the /sys directory
  # of the container is still mounted (that is kiwi's fault).
  sh('sudo rm -rf ' + build_dir, ignore_error = True)

def publish_container(container_name):
  """Publish the container on our internal docker registry."""

  if not container_exists(container_name):
    error("{0} container does not exist".format(container_name))
    sys.exit(1)

  sh("docker tag {0} {1}/{0}".format(container_name, DOCKER_REGISTRY_HOST))
  sh("docker push {0}/{1}".format(DOCKER_REGISTRY_HOST, container_name))

def sanitize_name(name):
  return name.replace('.', '_')

def extract_branches_from_options(options):
  branches = None
  try:
    branches = options.branches.split(',')
  except AttributeError:
    pass

  if branches:
    invalid_branches = set(branches) - set(GIT_BRANCH_BASE_CONTAINER.keys())
    if len(invalid_branches) != 0:
      print 'The following branches are invalid: '
      for branch in invalid_branches:
        print '  - ', branch
      print 'Allowed branches are: '
      for branch in GIT_BRANCH_BASE_CONTAINER.keys():
        print '  - ', branch
      sys.exit(1)
  else:
    branches = GIT_BRANCH_BASE_CONTAINER.keys()

  return branches

def extract_dbs_from_options(options):
  dbs = []

  try:
    dbs = options.databases.split(',')
  except AttributeError:
    pass

  if len(dbs):
    invalid_dbs = set(dbs) - set(KNOWN_DBS)
    if len(invalid_dbs) != 0:
      print 'The following dbs are invalid: '
      for db in invalid_dbs:
        print '  - ', db
      print 'Allowed dbs are: '
      for db in KNOWN_DBS:
        print '  - ', db
      sys.exit(1)

  return dbs

def extract_test_targets_from_options(options):
  test_targets = None
  try:
    test_targets = options.test_targets.split(',')
  except AttributeError:
    pass

  if test_targets:
    invalid_test_targets = set(test_targets) - set(TEST_TARGETS)
    if len(invalid_test_targets) != 0:
      print 'The following test targets are invalid: '
      for test_target in invalid_test_targets:
        print '  - ', test_target
      print 'Allowed test targets are: '
      for test_target in TEST_TARGETS:
        print '  - ', test_target
      sys.exit(1)
  else:
    test_targets = TEST_TARGETS

  return test_targets

def build_container_using_docker(container_name, parent_container, use_template_file = False, use_remote_parent = False):
  if use_remote_parent:
    parent_container = "{0}/{1}".format(DOCKER_REGISTRY_HOST, parent_container)
  elif not container_exists(parent_container):
    error('Parent container {0} does not exist'.format(parent_container))
    sys.exit(1)

  try:
    if use_template_file:
      sh("sed -e\"s/PARENT_CONTAINER/{0}/g\" Dockerfile.template > Dockerfile".format(re.escape(parent_container)))
    sh('docker build -rm=true -no-cache=true -t {0} .'.format(container_name))
  finally:
    if use_template_file and os.path.exists('Dockerfile'):
      os.remove('Dockerfile')

def is_db_supported_by_branch(db, branch):
    """Returns True if the db is supported by the target branch"""

    if not branch in INCOMPATIBLE_DBS_BY_BRANCH:
      return True

    unsupported_dbs = INCOMPATIBLE_DBS_BY_BRANCH[branch]

    return not db in unsupported_dbs
