#!/usr/bin/python
# pylint: disable=invalid-name

####
# 02/2006 Will Holcomb <wholcomb@gmail.com>
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
"""
Usage:
  Enables the use of multipart/form-data for posting forms

Inspirations:
  Upload files in python:
    http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/146306
  urllib2_file:
    Fabien Seisen: <fabien@seisen.org>

Example:
  import MultipartPostHandler, urllib2, cookielib

  cookies = cookielib.CookieJar()
  opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cookies),
                                MultipartPostHandler.MultipartPostHandler)
  params = { "username" : "bob", "password" : "riviera",
             "file" : open("filename", "rb") }
  opener.open("http://wwww.bobsite.com/upload/", params)

Further Example:
  The main function of this file is a sample which downloads a page and
  then uploads it to the W3C validator.
"""

try:
    from urllib.parse import urlencode
    from urllib.request import build_opener, HTTPHandler, BaseHandler
    from io import IOBase as file

    # pylint: disable-next=unused-import
    from io import BytesIO as StringIO
except ImportError:
    # pylint: disable-next=ungrouped-imports
    from urllib import urlencode
    from urllib2 import build_opener, HTTPHandler, BaseHandler

    # pylint: disable-next=unused-import
    from cStringIO import StringIO

import re
import random
import mimetypes

# pylint: disable-next=multiple-imports
import os, six, stat
import sys


class Callable:
    def __init__(self, anycallable):
        self.call = anycallable

    def __call__(self, *args, **kwargs):
        return self.call(*args, **kwargs)


# Controls how sequences are uncoded. If true, elements may be given multiple values by
#  assigning a sequence.
doseq = 1


# pylint: disable-next=missing-class-docstring
class MultipartPostHandler(BaseHandler):
    handler_order = HTTPHandler.handler_order - 10  # needs to run first

    def http_request(self, request):
        try:
            data = request.get_data()
        # pylint: disable-next=bare-except
        except:
            data = request.data
        # pylint: disable-next=unidiomatic-typecheck
        if data is not None and type(data) != str:
            v_files = []
            v_vars = []
            try:
                for key, value in list(data.items()):
                    if isinstance(value, file):
                        v_files.append((key, value))
                    else:
                        v_vars.append((key, value))
            except TypeError:
                # pylint: disable-next=unused-variable
                systype, value, traceback = sys.exc_info()
                six.reraise(
                    TypeError,
                    "not a valid non-string sequence or mapping object",
                    traceback,
                )

            if len(v_files) == 0:
                data = urlencode(v_vars, doseq)
            else:
                boundary, data = self.multipart_encode(v_vars, v_files)
                # pylint: disable-next=consider-using-f-string
                contenttype = "multipart/form-data; boundary=%s" % boundary
                if (
                    request.has_header("Content-Type")
                    and request.get_header("Content-Type").find("multipart/form-data")
                    != 0
                ):
                    print(
                        # pylint: disable-next=consider-using-f-string
                        "Replacing %s with %s"
                        % (request.get_header("content-type"), "multipart/form-data")
                    )
                request.add_unredirected_header("Content-Type", contenttype)

            try:
                request.add_data(data)
            # pylint: disable-next=bare-except
            except:
                request.data = data
        return request

    # pylint: disable-next=no-self-argument,redefined-builtin
    def multipart_encode(vars, files, boundary=None, buffer=None):
        if boundary is None:
            boundary = _make_boundary()
        if buffer is None:
            buffer = b""
        for key, value in vars:
            # pylint: disable-next=consider-using-f-string
            buffer += ("--%s\r\n" % boundary).encode()
            # pylint: disable-next=consider-using-f-string
            buffer += ('Content-Disposition: form-data; name="%s"' % key).encode()
            buffer += ("\r\n\r\n" + value + "\r\n").encode()
        for key, fd in files:
            # pylint: disable-next=unused-variable
            file_size = os.fstat(fd.fileno())[stat.ST_SIZE]
            filename = fd.name.split("/")[-1]
            contenttype = (
                mimetypes.guess_type(filename)[0] or "application/octet-stream"
            )
            # pylint: disable-next=consider-using-f-string
            buffer += ("--%s\r\n" % boundary).encode()
            buffer += (
                # pylint: disable-next=consider-using-f-string
                'Content-Disposition: form-data; name="%s"; filename="%s"\r\n'
                % (key, filename)
            ).encode()
            # pylint: disable-next=consider-using-f-string
            buffer += ("Content-Type: %s\r\n" % contenttype).encode()
            # buffer += 'Content-Length: %s\r\n' % file_size
            fd.seek(0)
            buffer += ("\r\n").encode() + fd.read() + ("\r\n").encode()
        # pylint: disable-next=consider-using-f-string
        buffer += ("--%s--\r\n\r\n" % boundary).encode()
        return boundary, buffer

    multipart_encode = Callable(multipart_encode)

    https_request = http_request


# This is a copy of email.generator._make_boundary
#
# The functionality to generate a boundary was previously provided my
# mimetools, but mimetools is deprecated in Python3 and there is no
# public replacement. So we copied this private method
def _make_boundary(text=None):
    # Craft a random boundary.  If text is given, ensure that the chosen
    # boundary doesn't appear in the text.
    # pylint: disable-next=invalid-name
    _width = len(repr(sys.maxsize - 1))
    # pylint: disable-next=invalid-name,consider-using-f-string
    _fmt = "%%0%dd" % _width
    token = random.randrange(sys.maxsize)
    boundary = ("=" * 15) + (_fmt % token) + "=="
    if text is None:
        return boundary
    b = boundary
    counter = 0
    while True:
        cre = _compile_re("^--" + re.escape(b) + "(--)?$", re.MULTILINE)
        if not cre.search(text):
            break
        b = boundary + "." + str(counter)
        counter += 1
    return b


# This is a copy of email.generator._compile_re
#
# See _make_boundary for further explainations.
def _compile_re(s, flags):
    return re.compile(s, flags)


def main():
    # pylint: disable-next=redefined-outer-name,reimported,import-outside-toplevel,multiple-imports
    import tempfile, sys

    # pylint: disable-next=invalid-name
    validatorURL = "http://validator.w3.org/check"
    opener = build_opener(MultipartPostHandler)

    # pylint: disable-next=invalid-name
    def validateFile(url):
        temp = tempfile.mkstemp(suffix=".html")
        os.write(temp[0], opener.open(url).read())
        params = {
            "ss": "0",  # show source
            "doctype": "Inline",
            "uploaded_file": open(temp[1], "rb"),
        }
        print(opener.open(validatorURL, params).read())
        os.remove(temp[1])

    if len(sys.argv[1:]) > 0:
        for arg in sys.argv[1:]:
            validateFile(arg)
    else:
        validateFile("http://www.google.com")


if __name__ == "__main__":
    main()
