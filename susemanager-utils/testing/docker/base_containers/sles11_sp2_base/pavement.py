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

CONTAINER_NAME = os.path.basename(os.path.dirname(os.path.abspath(__file__)))

@task
@cmdopts([
  ('build-only', 'B', 'Just build the container, do not import it locally')
  ])
def build(options):
  """Builds the container using kiwi and by default imports it into the local docker instance."""

  build_only = False
  try:
    build_only = options.build_only
  except AttributeError:
    pass

  build_utils.helpers.build_container_using_kiwi(
      os.path.dirname(os.path.abspath(__file__)),
      CONTAINER_NAME,
      build_only
  )

@task
def publish():
  """Publish the container on our internal docker registry."""

  build_utils.helpers.publish_container(CONTAINER_NAME)
