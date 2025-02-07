#!/usr/bin/env python3
"""Check when Promptail has finished processing logs"""

import os
import re
import time
import json
import requests

path_list = ""
positions_file = "/tmp/positions.yaml"


def exists_positions_file() -> bool:
    return os.path.exists(positions_file)


def complete() -> bool:
    print("[complete-checker] Checking if complete")
    fpath_pos_list = []
    while not os.path.exists(positions_file):
        print("[complete-checker] the positions file is not present yet")
        time.sleep(1)
    print("[complete-checker] The positions file is present!", flush=True)

    with open(positions_file, encoding="UTF-8") as f:
        print("[complete-checker] Processing positions file", flush=True)
        data = f.read()
        pattern = re.compile(r'(?:\s\s)([\w\/\.-]+\.log)\s*:\s*"(\d+)')
        fpath_pos_list = re.findall(pattern, data)

        for fpath_size in fpath_pos_list:
            log_file_path = fpath_size[0]
            log_file_pos = int(fpath_size[1])
            file_size = os.path.getsize(log_file_path)
            if log_file_pos != file_size:
                print(
                    f"[complete-checker] Last position {log_file_pos} of file (with size {file_size}) not reached yet for: {log_file_path}",
                    flush=True,
                )
                return False

    print("Promtail completed processing!", flush=True)
    return True


def push_flag_to_loki(
    loki_url="http://health_check_loki:3100",
    job_name="promtail-complete-job",
    flag="complete",
):

    log_entry = {
        "streams": [
            {
                "stream": {"job": job_name, "flag": flag},
                "values": [[str(int(time.time() * 1e9)), "Promtail finished!"]],
            }
        ]
    }

    response = requests.post(
        f"{loki_url}/loki/api/v1/push",
        headers={"Content-Type": "application/json"},
        data=json.dumps(log_entry),
    )

    if response.status_code == 204:
        print("[complete-checker] Flag log successfully pushed to Loki.", flush=True)
    else:
        print(
            "[complete-checker] Failed to push log to Loki:", response.text, flush=True
        )


if __name__ == "__main__":
    print("[complete-checker] Complete checker starting", flush=True)
    timeout = 60
    start_time = time.time()
    while True:
        elapsed_time = time.time() - start_time
        if elapsed_time >= timeout:
            print("Timeout waiting for Promtail to finish", flush=True)
            break

        if exists_positions_file():
            if complete():
                break
        time.sleep(10)
    print("[complete-checker] Pusing complete flag to Loki...", flush=True)
    push_flag_to_loki()
