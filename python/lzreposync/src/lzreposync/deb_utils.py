#  pylint: disable=missing-module-docstring


#  pylint: disable-next=missing-class-docstring
class LzDebHeader:
    # pylint: disable-next=pointless-string-statement

    def __init__(self, hdr):
        self.packaging = "deb"
        self.signatures = []
        self.is_source = 0
        self.deb = None
        self.hdr = hdr

    @staticmethod
    def is_signed():
        return 0

    def __getitem__(self, name):
        return self.hdr.get(str(name))

    def __setitem__(self, name, item):
        self.hdr[name] = item

    def __delitem__(self, name):
        del self.hdr[name]

    def __getattr__(self, name):
        return getattr(self.hdr, name)

    def __len__(self):
        return len(self.hdr)
