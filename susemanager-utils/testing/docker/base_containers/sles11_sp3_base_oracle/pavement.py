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
PARENT_CONTAINER = CONTAINER_NAME.rsplit('_', 1)[0]

@task
@cmdopts([
  ('use-remote-parent', 'r', 'Fetch the parent container from the private registry.')
  ])
def build():
  """Builds the container using docker."""

  use_remote_parent = False

  try:
    use_remote_parent = options.use_remote_parent
  except AttributeError:
    pass


  print 'Building', CONTAINER_NAME
  build_utils.helpers.build_container_using_docker(
    CONTAINER_NAME,
    PARENT_CONTAINER,
    use_template_file = True,
    use_remote_parent = use_remote_parent,
  )
  print 'Automated build done, now you have to execute the following steps:'
  print '1) Run the following command:'
  print '     docker run --privileged -t -i --rm -v <dir containing git checkout>:/manager {0} /bin/bash'.format(CONTAINER_NAME)
  print '2) From inside of the container run the following command:'
  print '     /manager/susemanager-utils/testing/docker/base_containers/sles11_sp3_base_oracle/setup-db-oracle.sh'
  print '3) Once the Oracle setup is done open a new terminal and run the following command:'
  print '     docker commit <id of the container> {0}'.format(CONTAINER_NAME)
  print '   The id of the container is the hostname of the running container. Otherwise you can obtain it by doing:'
  print '     docker ps'
  print "\nThese painful steps are going to disappear once docker's build system supports prileged containers."

@task
def publish(options):
  """Publish the container on our internal docker registry."""

  print 'Publishing {0} container'.format(CONTAINER_NAME)
  build_utils.helpers.publish_container(CONTAINER_NAME)
  print '{0} container successfully published.'.format(CONTAINER_NAME)
