# Copyright Notice:
# Copyright 2016-2019 DMTF. All rights reserved.
# License: BSD 3-Clause License. For full text see link: https://github.com/DMTF/python-redfish-library/blob/master/LICENSE.md

" Lib to receive ssdp packets "

import socket
import sys
import logging

# based on https://github.com/ZeWaren/python-upnp-ssdp-example/blob/master/lib/ssdp.py

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.INFO)
logger.addHandler(ch)

class RfSSDPServer():
    def addSearchTarget(self, target):
        self.searchtargets.append(target)

    def __init__(self, root, location, ip=None, port=1900, timeout=5):
        """__init__

        Initialize an SSDP server

        :param root: /redfish/v1 payload
        :param location: http location of server
        :param ip: address to bind to (IPV4 only?)
        :param port: port for server to exist on, default port 1900
        :param timeout: int for packet timeout
        """
        ip = ip if ip is not None else "0.0.0.0"
        self.searchtargets = ['ssdp:all', 'upnp:rootdevice', 'urn:dmtf-org:service:redfish-rest:1']
        self.ip, self.port = ip, port
        self.timeout = timeout

        # setup payload info
        self.location = location
        self.UUID = root.get('UUID', 'nouuid')
        self.cachecontrol = 1800
        myVersion = root.get('RedfishVersion', '1.0.0')
        self.major, self.minor, self.errata = tuple(myVersion.split('.'))
        self.addSearchTarget('urn:dmtf-org:service:redfish-rest:1:{}'.format(self.minor))

        # initiate multicast socket
        # rf-spec:
        #   must use TTL 2
        #   must use port 1900
        #   optional MSEARCH messages: Notify, Alive, Shutdown
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)  # ttl 2
        sock.settimeout(timeout)

        # To receive a multicast datagram, it is necessary to advise the kernel which
        # multicast groups we are interested in and ask the kernel to "join" those multicast groups
        # Depending on the underlying hardware, multicast datagrams are filtered
        # by the hardware or by the IP layer (and, in some cases, by both)
        # Only those with a destination group previously registered via a join are accepted
        # (ref: https://www.tldp.org/HOWTO/Multicast-HOWTO-2.html)
        # adding this membership, causes other applications to receive data from the kernel as well
        addr = socket.inet_aton('239.255.255.250')  # multicast address
        interface = socket.inet_aton(self.ip)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, addr + interface)

        sock.bind((self.ip, self.port))
        """
        Redfish Service Search Target (ST): "urn:dmtf-org:service:redfish-rest:1"
        For ssdp, "ssdp:all".
        For UPnP compatibility, the managed device should respond to MSEARCH
        queries searching for Search Target (ST) of "upnp:rootdevice"
        """
        self.sock = sock
        logger.info('SSDP Server Created')

    def start(self):
        logger.info('SSDP Server Running...')
        countTimeout = pcount = 0
        while True:
            try:
                if countTimeout % 5 == 0:
                    logger.info('Ssdp Poll... {} pings'.format(pcount))
                    pcount = 0
                    countTimeout = 1
                data, addr = self.sock.recvfrom(1024)
                pcount += 1
                self.check(data, addr)
            except socket.timeout:
                countTimeout += 1
                continue
            except Exception as e:
                logger.info('error occurred ' + str(e))
                pass
        pass

    def check(self, data, addr):
        logger.info('SSDP Packet received from {}'.format(addr))
        decoded = data.decode().replace('\r', '').split('\n')
        msgtype, decoded = decoded[0], decoded[1:]
        decodeddict = {x.split(':',  1)[0].upper(): x.split(':', 1)[1].strip(' ') for x in decoded if x != ''}

        if 'M-SEARCH' in msgtype:
            st = decodeddict.get('ST')
            if st in self.searchtargets:
                response = ['HTTP/1.1 200 OK',
                            'CACHE-CONTROL: max-age={}'.format(self.cachecontrol),
                            'ST:urn:dmtf-org:service:redfish-rest:1:{}'.format(self.minor),
                            'USN:uuid:{}::urn:dmtf-org:service:redfish-rest:1:{}'.format(self.UUID, self.minor),
                            'AL:{}'.format(self.location),
                            'EXT:']

                response.extend(('', ''))
                response = '\r\n'.join(response)

                self.sock.sendto(response.encode(), addr)
                logger.info('SSDP Packet sent to {}'.format(addr))


"""
example return payload
HTTP/1.1 200 OK
CACHE-CONTROL:max-age=<seconds, at least 1800>
ST:urn:dmtf-org:service:redfish-rest:1:<minor>
USN:uuid:<UUID of Manager>::urn:dmtf-org:service:redfish-rest:1:<minor>
AL:<URL of Redfish service root>
EXT:
"""


def main(argv=None):
    """
    main program
    """
    hostname = "127.0.0.1"
    location = "http://127.0.0.1"

    server = RfSSDPServer({}, '{}:{}{}'.format(location, '8000', '/redfish/v1'), hostname)

    try:
        server.start()
    except KeyboardInterrupt:
        pass

    # on exit will auto close sockets
    logger.info("Shutting down Ssdp server")
    sys.stdout.flush()


# the below is only executed if the program is run as a script
if __name__ == "__main__":
    sys.exit(main())
