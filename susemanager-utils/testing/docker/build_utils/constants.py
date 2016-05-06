GIT_BRANCH_BASE_CONTAINER = {
   '1.7' : 'sles11_sp2_base',
   'head' : 'sles11_sp3_base'
}

KNOWN_DBS = ('oracle', 'pgsql')

INCOMPATIBLE_DBS_BY_BRANCH = {
    '1.7' : ('oracle')
}

TEST_TARGETS = ('python', 'java')

DOCKER_REGISTRY_HOST = 'suma-docker-registry.mgr.suse.de'
