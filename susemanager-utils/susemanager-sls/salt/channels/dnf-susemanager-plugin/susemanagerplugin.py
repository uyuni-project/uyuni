import dnf  #  pylint: disable=missing-module-docstring

from dnfpluginscore import _, logger  #  pylint: disable=unused-import


class Susemanager(dnf.Plugin):  #  pylint: disable=missing-class-docstring
    name = "susemanager"

    def __init__(self, base, cli):  #  pylint: disable=useless-parent-delegation
        super(Susemanager, self).__init__(base, cli)

    def config(self):
        for repo in self.base.repos.get_matching("susemanager:*"):
            try:
                susemanager_token = repo.cfg.getValue(
                    section=repo.id, key="susemanager_token"
                )
                hdr = list(repo.get_http_headers())
                hdr.append("X-Mgr-Auth: %s" % susemanager_token)  #  pylint: disable=consider-using-f-string
                repo.set_http_headers(hdr)
                logger.debug(
                    "Susemanager Plugin: [%s] set token header: 'X-Mgr-Auth: ...%s'"  #  pylint: disable=consider-using-f-string
                    % (repo.id, susemanager_token[-10:])
                )
            except:  #  pylint: disable=bare-except
                pass
