import logging
import xml.sax.xmlreader


class PrimaryIncrementalParser(xml.sax.xmlreader.IncrementalParser):
    def __init__(self, handler):
        super().__init__()
        self._parser = xml.sax.make_parser()
        self._parser.setContentHandler(handler)
        self._parser.setFeature(xml.sax.handler.feature_namespaces, True)
        self._handler = handler

    def feed(self, data):
        self._parser.feed(data)
        if len(self._handler.batch) >= 19:  # TODO: change 19 by batch_size
            return self._handler.batch

    def close(self):
        logging.debug("Closing parser...")
        pass
