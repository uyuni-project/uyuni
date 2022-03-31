#!/usr/bin/env python3
# Copyright (c) 2022 SUSE LLC

import argparse
import logging
import os

import requests

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
    def __init__(self, server_addr, peer, path, options, root, url):
        self._root = root
        self._url = url
        super().__init__(server_addr, peer, path, options, None)

    def get_response_data(self):
        sep = "/"
        if self._path[0] == '/':
            sep = ""
        # accept only mac based config and defaults file
        if self._path.startswith("pxelinux.cfg/01-"):
            # request specifc system configuration
            logging.debug(f"Got request for {self._path}, forwarding to HTTP")
            return HttpResponseData("http://{}/tftp{}{}".format(self._url, sep, self._path))
        elif self._path.startswith("pxelinux.cfg/default"):
            # server local default
            logging.debug(f"Got request for {self._path}, forwarding to HTTP")
            return HttpResponseData("http://{}/tftp{}{}".format(self._url, sep, self._path))
        elif self._path.startswith("pxelinux.cfg/"):
            # ignore other pxelinux.cfg files
            logging.debug(f"Got request for {self._path}, ignoring")
            return StringResponseData("")
        # The rest get from http
        logging.debug(f"Got request for {self._path}, forwarding to HTTP")
        return HttpResponseData("http://{}/tftp{}{}".format(self._url, sep, self._path))

class TFTPServer(BaseServer):
    def __init__(self, address, port, retries, timeout, root, url):
        self._root = root
        self._url = url
        super().__init__(address, port, retries, timeout, None)

    def get_handler(self, server_addr, peer, path, options):
        return TFTPHandler(
            server_addr, peer, path, options, self._root, self._url
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
        "--configFile", type=str, default="/etc/rhn/tftp.conf", help="Path to configuration file"
    )
    return parser.parse_args()

def main():
    args = get_arguments()
    logging.getLogger().setLevel(logging.getLevelName(args.logLevel.upper()))

    try:
        with open(args.configFile) as config:
            pass
    except:
        logging.warning(f"Configuration file {args.configFile} missing, ignoring")
    
    server = TFTPServer(
        args.ip,
        args.port,
        args.retries,
        args.timeout,
        args.root,
        args.httpHost
    )
    try:
        server.run()
    except KeyboardInterrupt:
        server.close()


if __name__ == "__main__":
    main()
