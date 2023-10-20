import dnf

class Susemanager(dnf.Plugin):

    name = 'susemanager'

    def __init__(self, base, cli):
        super(Susemanager, self).__init__(base, cli)

    def config(self):
        for repo in self.base.repos.get_matching("susemanager:*"):
            try:
                susemanager_token = repo.cfg.getValue(section=repo.id, key="susemanager_token")
                hdr = list(repo.get_http_headers())
                hdr.append("X-Mgr-Auth: %s" % susemanager_token)
                repo.set_http_headers(hdr)
            except:
                pass
