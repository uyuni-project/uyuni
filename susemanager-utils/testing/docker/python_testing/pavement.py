from paver.easy import *
import os
import sys

sys.path.insert(0,
                os.path.abspath(
                  os.path.join(
                    os.path.dirname(os.path.abspath(__file__)),
                    '../')
                  )
               )

import build_utils.helpers
from build_utils.constants import *

CONTAINER_NAME_PREFIX = 'manager_' + os.path.basename(os.path.dirname(os.path.abspath(__file__)))

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default build vanilla container, without any db. The list is comma separated.')
  ])
def build(options):
  """Builds the container using docker."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  for branch in target_branches:
    if len(target_dbs):
      for db in target_dbs:
        print 'Building container for branch {0}, with {1} db'.format(branch, db)
        container_name = "{0}_{1}_{2}".format(CONTAINER_NAME_PREFIX, db, build_utils.helpers.sanitize_name(branch))
        parent_container = "{0}_{1}".format(GIT_BRANCH_BASE_CONTAINER[branch], db)
        build_utils.helpers.build_container_using_docker(
          container_name,
          parent_container,
          True
        )
        print '{0} container successfully built'.format(container_name)
    else:
      print 'Building vanilla container for branch', branch
      container_name = "{0}_{1}".format(CONTAINER_NAME_PREFIX, build_utils.helpers.sanitize_name(branch))
      parent_container = GIT_BRANCH_BASE_CONTAINER[branch]
      build_utils.helpers.build_container_using_docker(
        container_name,
        parent_container,
        True
      )
      print '{0} container successfully built'.format(container_name)

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
  ('databases=', 'd', 'Build container required to test the specified databases. By default build vanilla container, without any db. The list is comma separated.')
  ])
def publish(options):
  """Publish the container on our internal docker registry."""

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  for branch in target_branches:
    if len(target_dbs):
      for db in target_dbs:
        print 'Publishing container for branch {0}, with {1} db'.format(branch, db)
        container_name = "{0}_{1}_{2}".format(CONTAINER_NAME_PREFIX, db, build_utils.helpers.sanitize_name(branch))
        build_utils.helpers.publish_container(container_name)
        print '{0} container successfully published.'.format(container_name)
    else:
      print 'Publishing vanilla container for branch', branch
      container_name = "{0}_{1}".format(CONTAINER_NAME_PREFIX, build_utils.helpers.sanitize_name(branch))
      build_utils.helpers.publish_container(container_name)
      print '{0} container successfully published.'.format(container_name)
