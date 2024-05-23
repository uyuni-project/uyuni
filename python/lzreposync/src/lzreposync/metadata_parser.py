import xml.sax
import re


class PrimaryXmlHandler(xml.sax.ContentHandler):

    def __init__(self):
        self.current = ""  # current tag
        self.currentParentTreeStack = []  # useful for nested tags
        # self.count = 0  # only for testing, to limit the output

    def startElement(self, name, attrs):
        if name == "metadata":
            return  # We can remove the tag from the file (or a copy of it) prior processing to avoid making this test
        self.current = name
        self.currentParentTreeStack.append(name)
        # self.count += 1
        # print(60 * "-")
        print(
            f"current: [{self.current}] | attrs: { {attr: attrs[attr] for attr in attrs.getNames()} }"
        )
        print(f"Tag tree: {'/'.join(self.currentParentTreeStack)}")
        print(60 * "-")

    def startElementNS(self, name, qname, attrs):
        self.current = (
            qname  # TODO should we keep the namespace prefix ('rpm' for eg) or just
        )
        self.currentParentTreeStack.append(qname)
        # print(60 * "-")
        print(
            f"current: [{self.current}] | attrs: { {attr: attrs[attr] for attr in attrs.getNames()} }"
        )
        print(f"Tag tree: {'/'.join(self.currentParentTreeStack)}")

    def characters(self, content):
        if re.match("^[\w-]+$", content) is not None:  # alpha-num and dashes '-'
            print(f"current: [{self.current}]")
            print(f"Value: {content.strip()}")

    def endElement(self, name):
        # print(f"--End {self.current}")
        # if self.count == 100:
        #     exit(0)
        self.currentParentTreeStack.pop()
        print(60 * "-")

    def endElementNS(self, name, qname):
        self.currentParentTreeStack.pop()
        print(60 * "-")
