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

CONTAINER_NAME = os.path.basename(os.path.dirname(os.path.abspath(__file__)))
PARENT_CONTAINER = CONTAINER_NAME.rsplit('_', 1)[0] + '_base'

@task
def build():
  """Builds the container using docker."""

  print 'Building', CONTAINER_NAME
  build_utils.helpers.build_container_using_docker(CONTAINER_NAME, PARENT_CONTAINER)
  print '{0} container successfully built'.format(CONTAINER_NAME)

@task
def publish(options):
  """Publish the container on our internal docker registry."""

  print 'Publishing {0} container'.format(CONTAINER_NAME)
  build_utils.helpers.publish_container(CONTAINER_NAME)
  print '{0} container successfully published.'.format(CONTAINER_NAME)
