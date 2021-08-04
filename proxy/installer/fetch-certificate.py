#!/usr/bin/python -u

import os
import sys
import time
import argparse


RETRIES = 20
WAIT_RESPONSE = 10
REQUEST_TAG = 'suse/systemid/generate'
RESPONSE_TAG = 'suse/systemid/generated'


if __name__ == "__main__":
    try:
        import salt.config
        import salt.utils.event
    except ImportError as err:
        print("Unable to use Salt on this machine. Assuming traditional client.")
        sys.exit(0)

    parser = argparse.ArgumentParser()
    parser.add_argument('destination', default='/etc/sysconfig/rhn/systemid')
    args = parser.parse_args()
    opts = salt.config.minion_config('/etc/salt/minion', cache_minion_id=True)

    if not os.path.isdir(os.path.dirname(args.destination)):
        print("There is a problem with the provided destination.")
        sys.exit(1)

    event = salt.utils.event.get_event(
        'minion',
        sock_dir=opts['sock_dir'],
        transport=opts['transport'],
        listen=True,
        opts=opts)
    event.subscribe(tag=RESPONSE_TAG, match_type='fnmatch')

    for idx in range(RETRIES):
        print("Requesting certificate from server. [{0}/{1}]".format(idx+1, RETRIES))
        event.fire_master({}, REQUEST_TAG)  # send event to master
        data = event.get_event(
            full=False, auto_reconnect=True, no_block=False, match_type='fnmatch', tag=RESPONSE_TAG, wait=WAIT_RESPONSE)
        if data:
            try:
                with open(args.destination, 'wb') as _file:
                    _file.write(data['data'].encode('utf8'))
                    print("Certificate saved to: {0}".format(args.destination))
            except Exception as ex:
                print("Unable to write to destination: " + ex.message)
                sys.exit(1)
            sys.exit(0)
    print("Certificate not received from server. Exit.")
    sys.exit(1)
