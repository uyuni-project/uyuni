from yum.plugins import TYPE_CORE
from yum import config

requires_api_version = '2.5'
plugin_type = TYPE_CORE

def config_hook(conduit):
    config.RepoConf.susemanager_token = config.Option()

def prereposetup_hook(conduit):
    for repo in conduit.getRepos().listEnabled():
       susemanager_token = getattr(repo, 'susemanager_token', None)
       if susemanager_token:
          repo.http_headers['X-Mgr-Auth'] = susemanager_token
