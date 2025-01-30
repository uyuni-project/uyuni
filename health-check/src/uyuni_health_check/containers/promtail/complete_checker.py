#!/usr/bin/env python3

import os
import re
import time
import json
import requests
import logging


path_list= ""
positions_file = "/tmp/positions.yaml"

logging.basicConfig(filename='/var/log/complete_checker.log', level=logging.INFO)
logger = logging.getLogger(__name__)


def complete() -> bool:
    logger.info("Checking if complete")
    fpath_pos_list = []
    while not os.path.exists(positions_file):
        logger.info("the positions file is not present yet")
        time.sleep(1)
    logger.info("the positions file is present!")

        
    while True:
        with open(positions_file) as f:
            logger.info("before opening positions file")
            data = f.read()
            #fpath_pos_list = re.findall(r'([\w\/\.-]+\.log(?:\.gz)?)\s*:\s*"(\d+)"', data)
            fpath_pos_list = re.findall(r'([\w\/\.-]+\.log)\s*:\s*"(\d+)"', data)
            logger.info(f"matches in path and pos list: {fpath_pos_list}")
            if fpath_pos_list:
                break
            time.sleep(5)
            
    with open(positions_file) as f:
        for fpath_size in fpath_pos_list:
            log_file_path = fpath_size[0]
            log_file_pos = int(fpath_size[1])
            file_size = os.path.getsize(log_file_path)
            if log_file_pos != file_size:
                logging.info(f"Final of file not reached yet for: {log_file_path}")
                return False
            
    logging.info("Promtail completed processing!")
    return True

def push_flag_to_loki(loki_url="http://health_check_loki:3100", job_name="promtail-complete-job", flag="complete"):

    log_entry = {
        "streams": [
            {
                "stream": {
                    "job": job_name,
                    "flag": flag
                },
                "values": [
                    [str(int(time.time() * 1e9)), "Promtail finished!d"]
                ]
            }
        ]
    }

    response = requests.post(
        f"{loki_url}/loki/api/v1/push",
        headers={"Content-Type": "application/json"},
        data=json.dumps(log_entry)
    )

    if response.status_code == 204:
        print("Flag log successfully pushed to Loki.")
    else:
        print("Failed to push log to Loki:", response.text)

if __name__ == "__main__":

    logging.basicConfig(filename='/var/log/complete_checker.log', level=logging.INFO)
    logger = logging.getLogger(__name__)
    logger.info('Started')
    while(1):
        if complete():
            break
        time.sleep(10)

    push_flag_to_loki()

