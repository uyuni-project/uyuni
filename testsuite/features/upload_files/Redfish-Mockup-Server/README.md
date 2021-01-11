Copyright 2016-2020 DMTF. All rights reserved.

# The Redfish mockup server

The Redfish mockup server, `redfishMockupServer.py`, runs at a specified IP address and port or at the default IP address and port, `127.0.0.1:8000`, and serves Redfish GET, PATCH, POST, and DELETE requests and implements the `SubmitTestEvent` action.

## Prerequisite software

* **Python 3.4 or later**

    If Python 3.4 or later is not already installed, [download Python](https://www.python.org/downloads/ "https://www.python.org/downloads/") for your operating system.

    Verify the Python installation:
        
    ```
    $ python --version
    ```

    Ensure that Python 3.4 or later is in your path.
* **[pip](https://pip.pypa.io/en/stable/ "https://pip.pypa.io/en/stable/")**

    If pip is not installed, install it:
    
    ```
    $ curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
    $ python get-pip.py
    ```

    Upgrade pip and then verify the pip installation:
    
    ```
    $ pip install --upgrade pip
    $ pip --version
    ```
* **Required Python packages**

    Install the required Python packages:

    ```
    $ pip install -r requirements.txt
    ```

## Start the server

To start the server, run `redfishMockupServer.py` from your command shell:

```
$ python redfishMockupServer.py -D <DIR>
```

where

* `-D <DIR>` is the absolute or relative path to the mockup directory from the current working directory (CWD). Default is the CWD.

For example, if you copy `redfishMockupServer.py` to the `MyServerMockup9` folder, run this command to start the server on port 8001:

```
$ python redfishMockupServer.py -p 8001 -D ./MyServerMockup9
```

> **Note:** You can run the server can accept *tall* or *short* mockups:
> 
> | Form  | Description | Note |
> | :---  | :---        | :--- |
> | Tall  | The version resource, `/redfish`, is at the top of the mockup directory structure. | Default is tall form. |
> | Short | The service root resource, `/redfish/v1/`, is at the top of the mockup directory structure. | Use the `-S` option to run in short form. |

## redfishMockupServer usage

```
usage: redfishMockupServer.py [-h] [-H HOST] [-p PORT] [-D DIR] [-E] [-X]
                              [-t TIME] [-T] [-s] [--cert CERT] [--key KEY]
                              [-S] [-P]

Serve a static Redfish mockup.

optional arguments:
  -h, --help            show this help message and exit
  -H HOST, --host HOST, --Host HOST
                        hostname or IP address (default 127.0.0.1)
  -p PORT, --port PORT, --Port PORT
                        host port (default 8000)
  -D DIR, --dir DIR, --Dir DIR
                        path to mockup dir (may be relative to CWD)
  -E, --test-etag, --TestEtag
                        (unimplemented) etag testing
  -X, --headers         load headers from headers.json files in mockup
  -t TIME, --time TIME  delay in seconds added to responses (float or int)
  -T                    delay response based on times in time.json files in
                        mockup
  -s, --ssl             place server in SSL (HTTPS) mode; requires a cert and
                        key
  --cert CERT           the certificate for SSL
  --key KEY             the key for SSL
  -S, --short-form, --shortForm
                        apply short form to mockup (omit filepath /redfish/v1)
  -P, --ssdp            make mockup SSDP discoverable
```

## Release process

To create a release of the Redfish mockup server:

1. Update `CHANGELOG.md` with the list of changes since the last release.
2. Update the `tool_version` variable in `redfishMockupServer.py` to the new version of the tool.
3. Push changes to GitHub.
4. Create a release in GitHub.
