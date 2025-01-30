#!/usr/bin/env python3.11
"""Manage the Promptail process"""

import subprocess
import time

def is_process_running(process_name):
    try:
        subprocess.run(["pgrep", "-f", process_name], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except subprocess.CalledProcessError:
        return False

def launch_process(command):
    return subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

promtail_command = "promtail --config.file=/etc/promtail/config.yml"
promtail_process = launch_process(promtail_command)

python_script_process = launch_process("python3.11 /usr/bin/complete_checker.py")

while True:
    # Check if the promtail process is still running, relaunch if not
    if not is_process_running("promtail"):
        print("Promtail process is not running. Relaunching...")
        promtail_process = launch_process(promtail_command)

    # Delay between checks to prevent constant CPU usage
    time.sleep(10)
