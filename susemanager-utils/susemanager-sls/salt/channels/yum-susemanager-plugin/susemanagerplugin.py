from yum.plugins import TYPE_CORE  #  pylint: disable=missing-module-docstring
from yum import config

requires_api_version = "2.5"
plugin_type = TYPE_CORE


def config_hook(conduit):  #  pylint: disable=unused-argument
    config.RepoConf.susemanager_token = config.Option()


def init_hook(conduit):
    for repo in conduit.getRepos().listEnabled():
        susemanager_token = getattr(repo, "susemanager_token", None)
        if susemanager_token:
            repo.http_headers["X-Mgr-Auth"] = susemanager_token
