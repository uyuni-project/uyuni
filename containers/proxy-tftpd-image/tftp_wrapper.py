#!/usr/bin/env python3
# Copyright (c) 2022 SUSE LLC

import argparse
import logging
import os
import requests
import yaml

from fbtftp.base_handler import BaseHandler
from fbtftp.base_handler import ResponseData
from fbtftp.base_handler import StringResponseData
from fbtftp.base_server import BaseServer

class FileResponseData(ResponseData):
    def __init__(self, path):
        self._size = os.stat(path).st_size
        self._reader = open(path, "rb")

    def read(self, n):
        return self._reader.read(n)

    def size(self):
        return self._size

    def close(self):
        self._reader.close()

class HttpResponseData(ResponseData):
    def __init__(self, url):
        # request file by url and store it
        self._request = requests.get(url, stream=True)
        self._stream = self._request.iter_content(chunk_size=1024)
        self._content = b''
    def read(self, requested):
        data = self._content
        read = len(self._content)
        while read < requested:
           try:
              d = next(self._stream)
              data += d
              read += len(d)
           except StopIteration:
              break
        if read > requested:
           self._content = data[requested:]
           data = data[:requested]
        else:
           self._content = b''
        return data
    def size(self):
        return int(self._request.headers['content-length'])
    def close(self):
        self._request.close()

class TFTPHandler(BaseHandler):
    def __init__(self, server_addr, peer, path, options, root, url, saltboot):
        self._root = root
        self._url = url
        self._saltboot = saltboot
        super().__init__(server_addr, peer, path, options, None)

    def get_response_data(self):
        path = self._path
        if self._path[0] == '/':
            path = self._path[1:]

        # accept only mac based config and defaults file
        if path.startswith("pxelinux.cfg/01-"):
            # request specific system configuration
            logging.debug(f"Got request for {path}, forwarding to HTTP")
            return HttpResponseData("http://{}/tftp/{}".format(self._url, path))
        elif path.startswith("pxelinux.cfg/default"):
            # server local default
            logging.debug(f"Got request for {path}, forwarding to HTTP")
            if self._saltboot:
                # saltboot mode use proxy fqdn filename in saltboot directory to get custom saltboot
                # group defaults
                return HttpResponseData("http://{}/tftp/saltboot/{}".format(self._url, self._url))
            return HttpResponseData("http://{}/tftp/{}".format(self._url, path))
        elif path.startswith("pxelinux.cfg/"):
            # ignore other pxelinux.cfg files
            logging.debug(f"Got request for {path}, ignoring")
            return StringResponseData("")
        # The rest get from http
        logging.debug(f"Got request for {path}, forwarding to HTTP")
        return HttpResponseData("http://{}/tftp/{}".format(self._url, path))

class TFTPServer(BaseServer):
    def __init__(self, address, port, retries, timeout, root, url, saltboot):
        self._root = root
        self._url = url
        self._saltboot = saltboot
        super().__init__(address, port, retries, timeout, None)

    def get_handler(self, server_addr, peer, path, options):
        return TFTPHandler(
            server_addr, peer, path, options, self._root, self._url, self._saltboot
        )

def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", type=str, default="::", help="IP address to bind to")
    parser.add_argument("--port", type=int, default=69, help="port to bind to")
    parser.add_argument(
        "--retries", type=int, default=5, help="Number of per-packet retries"
    )
    parser.add_argument(
        "--timeout", type=int, default=2, help="Timeout for packet retransmission in seconds"
    )
    parser.add_argument(
        "--root", type=str, default="/srv/tftpboot", help="Root of the static filesystem"
    )
    parser.add_argument(
        "--httpHost", type=str, default="localhost", help="Hostname where to forward HTTP requests"
    )
    parser.add_argument(
        "--logLevel", choices=['error', 'warning', 'info', 'debug'], default="error", help="Logging level for the server"
    )
    parser.add_argument(
        "--configFile", type=str, default="/etc/uyuni/config.yaml", help="Path to configuration file"
    )
    return parser.parse_args()

def main():
    args = get_arguments()
    logging.getLogger().setLevel(logging.getLevelName(args.logLevel.upper()))

    try:
        with open(args.configFile) as source:
            config = yaml.safe_load(source)
            args.httpHost = config['proxy_fqdn']
            args.saltboot = config.get('saltboot_mode', True)

    except IOError as err:
        logging.warning(f"Configuration file reading error {err}, ignoring")
    except KeyError as err:
        logging.warning(f"Invalid configuration file passed, missing {err}")
    
    server = TFTPServer(
        args.ip,
        args.port,
        args.retries,
        args.timeout,
        args.root,
        args.httpHost,
        args.saltboot
    )
    try:
        server.run()
    except KeyboardInterrupt:
        server.close()

if __name__ == "__main__":
    main()
