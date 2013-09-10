from paver.easy import *
import os
import sys

sys.path.insert(0,
                os.path.abspath(
                  os.path.join(
                    os.path.dirname(os.path.abspath(__file__)),
                    './')
                  )
               )


import build_utils.helpers
from build_utils.constants import *

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.abspath(__file__)))

@task
@cmdopts([
  ('build-only', 'B', 'Just build the container, do not import it locally.'),
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
])
def build_base_containers():
  """Build the base containers. Note well: kiwi is required."""
  build_only = False
  try:
    build_only = options.build_only
  except AttributeError:
    pass

  target_branches = build_utils.helpers.extract_branches_from_options(options)

  for branch in target_branches:
    container_name = GIT_BRANCH_BASE_CONTAINER[branch]
    container_project_dir = os.path.join(ROOT_DIR, 'base_containers', container_name)
    print 'Building {0} container required by branch {1}'.format(container_name, branch)

    with pushd(container_project_dir):
      cmd = 'paver build'
      if build_only:
        cmd += ' -B'
      sh(cmd)

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
])
def publish_base_containers():
  """Publish the base containers."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)

  for branch in target_branches:
    container_name = GIT_BRANCH_BASE_CONTAINER[branch]
    container_project_dir = os.path.join(ROOT_DIR, 'base_containers', container_name)
    print 'Publishing {0} container required by branch {1}'.format(container_name, branch)

    with pushd(container_project_dir):
      sh('paver publish')

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default builds containers for all the dbs. The list is comma separated.')
])
def build_db_containers(options):
  """Build the container using docker."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  if not len(target_dbs):
    target_dbs = KNOWN_DBS

  for branch in target_branches:
    for db in target_dbs:
      container_name = GIT_BRANCH_BASE_CONTAINER[branch] + "_" + db
      container_project_dir = os.path.join(ROOT_DIR, 'base_containers', container_name)
      print 'Building {0} container required by branch {1} and by db {2}'.format(container_name, branch, db)

      with pushd(container_project_dir):
        sh('paver build')

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default builds containers for all the dbs. The list is comma separated.')
])
def publish_db_containers(options):
  """Publish the db containers."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  if not len(target_dbs):
    target_dbs = KNOWN_DBS

  for branch in target_branches:
    for db in target_dbs:
      container_name = GIT_BRANCH_BASE_CONTAINER[branch] + "_" + db
      container_project_dir = os.path.join(ROOT_DIR, 'base_containers', container_name)
      print 'Publishing {0} container required by branch {1} and by db {2}'.format(container_name, branch, db)

      with pushd(container_project_dir):
        sh('paver publish')

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default builds containers for all the dbs. The list is comma separated.'),
  ('test-target=', 't', 'Build container required to test the specified targets. By default builds containers for all the test targets. The list is comma separated.')
])
def build_testing_containers(options):
  """Build the container used to run SUSE Manager's tests."""

  target_branches = ','.join(build_utils.helpers.extract_branches_from_options(options))
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)
  test_targets    = build_utils.helpers.extract_test_targets_from_options(options)

  if not len(target_dbs):
    target_dbs = KNOWN_DBS

  target_dbs = ','.join(target_dbs)

  for test_target in test_targets:
    container_project_dir = os.path.join(ROOT_DIR, test_target + '_testing')

    with pushd(container_project_dir):
      sh('paver build -b {0} -d {1}'.format(target_branches, target_dbs))

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default builds containers for all the dbs. The list is comma separated.'),
  ('test-target=', 't', 'Build container required to test the specified targets. By default builds containers for all the test targets. The list is comma separated.')
])
def publish_testing_containers(options):
  """Publish the container used to run SUSE Manager's tests."""

  target_branches = ','.join(build_utils.helpers.extract_branches_from_options(options))
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)
  test_targets    = build_utils.helpers.extract_test_targets_from_options(options)

  if not len(target_dbs):
    target_dbs = KNOWN_DBS

  target_dbs = ','.join(target_dbs)

  for test_target in test_targets:
    container_project_dir = os.path.join(ROOT_DIR, test_target + '_testing')

    with pushd(container_project_dir):
      sh('paver publish -b {0} -d {1}'.format(target_branches, target_dbs))

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default builds containers for all the dbs. The list is comma separated.'),
  ('test-target=', 't', 'Build container required to test the specified targets. By default builds containers for all the test targets. The list is comma separated.')
])
def pull_all_containers(options):
  """Publish the container used to run SUSE Manager's tests."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)
  test_targets    = build_utils.helpers.extract_test_targets_from_options(options)

  if not len(target_dbs):
    target_dbs = KNOWN_DBS

  for branch in target_branches:
    container_name = GIT_BRANCH_BASE_CONTAINER[branch]
    sh('docker pull {0}/{1}'.format(DOCKER_REGISTRY_HOST, container_name), ignore_error = True)
    for db in target_dbs:
      container_to_pull = "{0}_{1}".format(container_name, db)
      sh('docker pull {0}/{1}'.format(DOCKER_REGISTRY_HOST, container_to_pull), ignore_error = True)

      for target in test_targets:
        # the name of the container is something like manager_python_testing_pgsql_1_7
        container_to_pull = "manager_{0}_testing_{1}_{2}".format(
            target,
            db,
            build_utils.helpers.sanitize_name(branch)
        )
        sh('docker pull {0}/{1}'.format(DOCKER_REGISTRY_HOST, container_to_pull), ignore_error = True)

