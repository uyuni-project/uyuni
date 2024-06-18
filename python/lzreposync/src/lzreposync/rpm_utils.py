# TODO: find a better location for this class (beware of circular import)
class RPMHeader:
    """
    RPM Pacakge Header
    """

    def __init__(self, is_source=False, packaging="rpm"):
        self.is_source = is_source
        self.packaging = packaging
