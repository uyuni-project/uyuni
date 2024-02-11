#  pylint: disable=missing-module-docstring
import dnf

# pylint: disable-next=unused-import
from dnfpluginscore import _, logger


# pylint: disable-next=missing-class-docstring
class Susemanager(dnf.Plugin):
    name = "susemanager"

    # pylint: disable-next=useless-parent-delegation
    def __init__(self, base, cli):
        super(Susemanager, self).__init__(base, cli)

    def config(self):
        for repo in self.base.repos.get_matching("susemanager:*"):
            try:
                susemanager_token = repo.cfg.getValue(
                    section=repo.id, key="susemanager_token"
                )
                hdr = list(repo.get_http_headers())
                # pylint: disable-next=consider-using-f-string
                hdr.append("X-Mgr-Auth: %s" % susemanager_token)
                repo.set_http_headers(hdr)
                logger.debug(
                    # pylint: disable-next=consider-using-f-string
                    "Susemanager Plugin: [%s] set token header: 'X-Mgr-Auth: ...%s'"
                    % (repo.id, susemanager_token[-10:])
                )
            # pylint: disable-next=bare-except
            except:
                pass
