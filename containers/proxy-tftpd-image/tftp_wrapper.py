#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2024 SUSE LLC
#
# SPDX-License-Identifier: MIT

import argparse
import logging
import os
import requests
import yaml

from fbtftp.base_handler import BaseHandler
from fbtftp.base_handler import ResponseData
from fbtftp.base_handler import StringResponseData
from fbtftp.base_server import BaseServer


def stats(s):
    pass


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
    def __init__(self, url, capath):
        # request file by url and store it
        self._request = requests.get(url, stream=True, verify=capath)
        if self._request.status_code == 404:
            raise FileNotFoundError()
        self._request.raise_for_status()
        self._stream = self._request.iter_content(chunk_size=1024)
        self._content = b""
        self._size = int(self._request.headers["content-length"])

    def read(self, requested):
        data = self._content
        read = len(self._content)
        while read < requested:
            if self._stream is None:
                break
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
            self._content = b""
        return data

    def size(self):
        return self._size

    def close(self):
        self._request.close()


class HttpResponseDataFiltered(HttpResponseData):
    """
    Filter reponse entries based on supplied fqdn option

    Saltboot entries have MASTER= option, we want only those mathing our proxy
    """

    def __init__(self, url, capath, proxyFqdn, serverFqdn):
        # request file by url and store it
        self._proxyFqdn = proxyFqdn
        self._serverFqdn = serverFqdn
        self._request = requests.get(url, stream=True, verify=capath)
        if self._request.status_code == 404:
            raise FileNotFoundError()
        self._request.raise_for_status()

        self._stream = None
        self._content = self._request.content
        self.contentFilter()
        self._size = len(self._content)

    def contentFilter(self):
        raise NotImplementedError()


class HttpResponseDataFilteredPXE(HttpResponseDataFiltered):
    def contentFilter(self):
        saltboot_content = ""
        cobbler_content = ""
        have_entry = False
        entry = ""
        entry_name = ""
        in_entry = False
        for line in self._content.decode("utf-8").splitlines():
            if in_entry:
                if line.startswith(" ") or line.startswith("\t"):
                    entry += line + "\n"
                else:
                    in_entry = False
                    # detect Saltboot entries
                    if (
                        " MASTER=" + self._proxyFqdn in entry
                        and "MINION_ID_PREFIX" in entry
                    ):
                        have_entry = True
                        entry += "        MENU DEFAULT\n"
                        entry += f"ONTIMEOUT {entry_name}\n"
                        saltboot_content += entry
                    else:
                        cobbler_content += entry.replace(
                            self._serverFqdn, self._proxyFqdn
                        )
                    entry = ""
            if not in_entry:
                if line.startswith("LABEL"):
                    entry = line + "\n"
                    entry_name = line.split(" ", 2)[1]
                    in_entry = True
                else:
                    if not line.startswith(
                        "ONTIMEOUT"
                    ):  # ONTIMEOUT points to deleted entry
                        saltboot_content += line + "\n"
                        cobbler_content += line + "\n"
        if have_entry:
            self._content = saltboot_content.encode("utf-8")
        else:
            self._content = cobbler_content.encode("utf-8")
        logging.debug("translation finished")


class HttpResponseDataFilteredGrub(HttpResponseDataFiltered):
    def contentFilter(self):
        saltboot_content = ""
        cobbler_content = ""
        have_entry = False
        entry = ""
        entry_name = ""
        in_entry = False
        for line in self._content.decode("utf-8").splitlines():
            if in_entry:
                entry += line + "\n"
                if line.rstrip().endswith("}"):
                    in_entry = False
                    # detect Saltboot entries
                    if (
                        " MASTER=" + self._proxyFqdn in entry
                        and "MINION_ID_PREFIX" in entry
                    ):
                        have_entry = True
                        saltboot_content += entry
                        saltboot_content += f"set default={entry_name}\n"
                    else:
                        cobbler_content += entry.replace(
                            self._serverFqdn, self._proxyFqdn
                        )
                    entry = ""
            else:
                if line.startswith("menuentry"):
                    entry_name = line.split(" ", 3)[1]
                    entry = line + "\n"
                    in_entry = True
                else:
                    saltboot_content += line + "\n"
                    cobbler_content += line + "\n"
        if have_entry:
            self._content = saltboot_content.encode("utf-8")
        else:
            self._content = cobbler_content.encode("utf-8")


class TFTPHandler(BaseHandler):
    def __init__(
        self,
        server_addr,
        peer,
        path,
        options,
        root,
        httpHost,
        proxyFqdn,
        serverFqdn,
        capath,
    ):
        self._root = root
        self._httpHost = httpHost
        self._proxyFqdn = proxyFqdn
        self._serverFqdn = serverFqdn
        self._capath = capath
        super().__init__(server_addr, peer, path, options, stats)

    def get_response_data(self):
        capath = self._capath
        target = self._httpHost
        path = self._path
        if self._path[0] == "/":
            path = self._path[1:]

        # accept only mac based config and defaults file
        if path.startswith("pxelinux.cfg/01-"):
            # request specific system configuration
            logging.debug(f"Got request for system PXE {path}, filtering HTTP")
            return HttpResponseDataFilteredPXE(
                f"{target}/tftp/{path}", capath, self._proxyFqdn, self._serverFqdn
            )
        elif path.startswith("grub/system"):
            logging.debug(f"Got request for system GRUB {path}, filtering HTTP")
            return HttpResponseDataFilteredGrub(
                f"{target}/tftp/{path}", capath, self._proxyFqdn, self._serverFqdn
        elif path.startswith("pxelinux.cfg/default"):
            # server local default
            logging.debug(f"Got request for {path}, filtering HTTP")
            return HttpResponseDataFilteredPXE(
                f"{target}/tftp/{path}", capath, self._proxyFqdn, self._serverFqdn
            )
        elif path.startswith("pxelinux.cfg/"):
            # ignore other pxelinux.cfg files
            logging.debug(f"Got request for {path}, ignoring")
            raise FileNotFoundError()
        elif path.startswith("grub/") and path.endswith("_menu_items.cfg"):
            logging.debug(f"Got request for {path}, filtering HTTP")
            return HttpResponseDataFilteredGrub(
                f"{target}/tftp/{path}", capath, self._proxyFqdn, self._serverFqdn
            )
        # The rest get from http
        logging.debug(f"Got request for {path}, forwarding to HTTP")
        return HttpResponseData(f"{target}/tftp/{path}", capath)


class TFTPServer(BaseServer):
    def __init__(
        self,
        address,
        port,
        retries,
        timeout,
        root,
        httpHost,
        proxyFqdn,
        serverFqdn,
        capath,
    ):
        self._root = root
        if capath is None or httpHost == "localhost":
            self._httpHost = f"http://{httpHost}"
            logging.info("SSL not used for inproxy communication")
        else:
            self._httpHost = f"https://{httpHost}"
            logging.info("HTTPS used for inproxy communication")

        self._proxyFqdn = proxyFqdn
        self._serverFqdn = serverFqdn
        self._capath = capath
        super().__init__(address, port, retries, timeout, None)

    def get_handler(self, server_addr, peer, path, options):
        return TFTPHandler(
            server_addr,
            peer,
            path,
            options,
            self._root,
            self._httpHost,
            self._proxyFqdn,
            self._serverFqdn,
            self._capath,
        )


def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--ip", type=str, default="0.0.0.0", help="IP address to bind to"
    )
    parser.add_argument("--port", type=int, default=69, help="port to bind to")
    parser.add_argument(
        "--retries", type=int, default=5, help="Number of per-packet retries"
    )
    parser.add_argument(
        "--timeout",
        type=int,
        default=2,
        help="Timeout for packet retransmission in seconds",
    )
    parser.add_argument(
        "--root",
        type=str,
        default="/srv/tftpboot",
        help="Root of the static filesystem",
    )
    parser.add_argument(
        "--httpHost",
        type=str,
        default="localhost",
        help="Hostname where to forward HTTP requests",
    )
    parser.add_argument(
        "--serverFqdn",
        type=str,
        default="localhost",
        help="Hostname of the server for cobbler filtering",
    )
    parser.add_argument(
        "--proxyFqdn",
        type=str,
        help="Hostname of the proxy for retail filtering",
        default="localhost",
    )
    parser.add_argument(
        "--logLevel",
        choices=["error", "warning", "info", "debug"],
        default="info",
        help="Logging level for the server",
    )
    parser.add_argument(
        "--configFile",
        type=str,
        default="/etc/uyuni/config.yaml",
        help="Path to configuration file",
    )
    parser.add_argument(
        "--caPath",
        type=str,
        default="/usr/share/uyuni/ca.crt",
        help="Path to CA certificate",
    )
    return parser.parse_args()


def main():
    args = get_arguments()
    logging.getLogger().setLevel(logging.getLevelName(args.logLevel.upper()))

    try:
        with open(args.configFile) as source:
            config = yaml.safe_load(source)
            args.proxyFqdn = config["proxy_fqdn"]
            args.serverFqdn = config["server"]
            log_level = (
                logging.DEBUG if config.get("log_level", 1) == 5 else logging.INFO
            )
            logging.getLogger().setLevel(log_level)

    except IOError as err:
        logging.warning(f"Configuration file reading error {err}, ignoring")
    except KeyError as err:
        logging.error(f"Invalid configuration file passed, missing {err}")
        exit(1)

    logging.info("Starting TFTP proxy:")
    logging.info(f"HTTP endpoint: {args.httpHost}")
    logging.info(f"Server FQDN: {args.serverFqdn}")
    logging.info(f"Proxy FQDN: {args.proxyFqdn}")
    logging.info(f"CA path: {args.caPath}")

    server = TFTPServer(
        args.ip,
        args.port,
        args.retries,
        args.timeout,
        args.root,
        args.httpHost,
        args.proxyFqdn,
        args.serverFqdn,
        args.caPath,
    )

    try:
        server.run()
    except KeyboardInterrupt:
        server.close()


if __name__ == "__main__":
    main()
