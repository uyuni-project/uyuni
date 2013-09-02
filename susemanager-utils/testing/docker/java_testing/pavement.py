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

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.abspath(__file__)))

@task
@cmdopts([
  ('branches=', 'b', 'Build container required to test the specified branches. By default build containers for all the branches. The list is comma separated.'),
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

  target_branches = build_utils.helpers.extract_branches_from_options(options)
  target_dbs      = build_utils.helpers.extract_dbs_from_options(options)

  for branch in target_branches:
    container_project_dir = os.path.join(ROOT_DIR, branch)
    with pushd(container_project_dir):
      cmd = 'paver build'
      if use_remote_parent:
        cmd += ' -r'
      if len(target_dbs):
        cmd += ' -d ' + ",".join(target_dbs)
      sh(cmd)

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
    container_project_dir = os.path.join(ROOT_DIR, branch)
    with pushd(container_project_dir):
      cmd = 'paver publish'
      if len(target_dbs):
        cmd += ' -d ' + ",".join(target_dbs)
      sh(cmd)
