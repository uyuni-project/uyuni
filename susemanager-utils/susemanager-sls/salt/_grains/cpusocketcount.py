#!/usr/bin/env python
import logging
import re

log = logging.getLogger(__name__)

def cpusockets():
  p = re.compile("physical id\\s+:\\s+(\\d+)")
  grains = {}
  physids = {}
  with open('/proc/cpuinfo') as f:
    for line in f:
      if line.strip().startswith('physical id'):
        comps = line.split(':')
        if not len(comps) > 1:
          continue
        if not len(comps[1]) > 1:
          continue
        val = comps[1].strip()
        physids[val] = True
  # on VMs with 1 vCPU assigned, there is no "physcal id" in cpuinfo
  grains['cpusockets'] = len(physids) if len(physids) > 0 else 1
  return grains

#if __name__ == "__main__":
#  print "sockets={}".format(cpusockets()['cpusockets'])

