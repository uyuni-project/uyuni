from yum.plugins import TYPE_CORE
from yum import config

requires_api_version = '2.5'
plugin_type = TYPE_CORE

def config_hook(conduit):
    config.RepoConf.suma_token = config.Option()

def prereposetup_hook(conduit):
    for repo in conduit.getRepos().listEnabled():
       suma_token = getattr(repo, 'suma_token', None)
       if suma_token:
          repo.http_headers['X-SUMA-TOKEN'] = suma_token
