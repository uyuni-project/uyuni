from paver.easy import *
import os
import sys

sys.path.insert(0,
                os.path.abspath(
                  os.path.join(
                    os.path.dirname(os.path.abspath(__file__)),
                    '../../')
                  )
               )

import build_utils.helpers
from build_utils.constants import *

CONTAINER_NAME_PREFIX = 'manager_java_testing'
BRANCH = os.path.basename(os.path.dirname(os.path.abspath(__file__)))

@task
@cmdopts([
  ('databases=', 'd', 'Build container required to test the specified databases. By default build vanilla container, without any db. The list is comma separated.'),
  ('use-remote-parent', 'r', 'Fetch the parent container from the private registry.')
])
def build(options):
  """Builds the container using docker."""

  use_remote_parent = False
  try:
    use_remote_parent = options.use_remote_parent
  except AttributeError:
    pass

  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  if len(target_dbs):
    for db in target_dbs:
      print 'Building container for branch {0}, with {1} db'.format(BRANCH, db)
      container_name = "{0}_{1}_{2}".format(CONTAINER_NAME_PREFIX, db, build_utils.helpers.sanitize_name(BRANCH))
      parent_container = "{0}_{1}".format(GIT_BRANCH_BASE_CONTAINER[BRANCH], db)
      build_utils.helpers.build_container_using_docker(
        container_name,
        parent_container,
        use_template_file = True,
        use_remote_parent = use_remote_parent
      )
      print '{0} container successfully built'.format(container_name)
  else:
    print 'Building vanilla container for branch', BRANCH
    container_name = "{0}_{1}".format(CONTAINER_NAME_PREFIX, build_utils.helpers.sanitize_name(BRANCH))
    parent_container = GIT_BRANCH_BASE_CONTAINER[BRANCH]
    build_utils.helpers.build_container_using_docker(
      container_name,
      parent_container,
      use_template_file = True,
      use_remote_parent = use_remote_parent
    )
    print '{0} container successfully built'.format(container_name)

@task
@cmdopts([
  ('databases=', 'd', 'Build container required to test the specified databases. By default build vanilla container, without any db. The list is comma separated.')
])
def publish(options):
  """Publish the container on our internal docker registry."""

  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  if len(target_dbs):
    for db in target_dbs:
      print 'Publishing container for branch {0}, with {1} db'.format(BRANCH, db)
      container_name = "{0}_{1}_{2}".format(CONTAINER_NAME_PREFIX, db, build_utils.helpers.sanitize_name(BRANCH))
      build_utils.helpers.publish_container(container_name)
      print '{0} container successfully published.'.format(container_name)
  else:
    print 'Publishing vanilla container for branch', BRANCH
    container_name = "{0}_{1}".format(CONTAINER_NAME_PREFIX, build_utils.helpers.sanitize_name(BRANCH))
    build_utils.helpers.publish_container(container_name)
    print '{0} container successfully published.'.format(container_name)
