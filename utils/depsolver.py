#!/usr/bin/python
#
# -*- coding: utf-8 -*-
#
# Copyright (c) 2012--2017 Red Hat, Inc.
#
# Lookup package dependencies in a yum repository
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation

import logging
import sys
import solv
import os
import yaml

from optparse import OptionParser, Option

try:
    from spacewalk.satellite_tools.progress_bar import ProgressBar
except ImportError:
    # pylint: disable=F0401
    _LIBPATH = "/usr/share/rhn"
    if _LIBPATH not in sys.path:
        sys.path.append(_LIBPATH)
    from satellite_tools.progress_bar import ProgressBar


log = logging.getLogger(__name__)

CACHE_DIR = "/tmp/cache/yum"
PERSIST_DIR = "/var/lib/yum"


class DepSolver:

    def __init__(self, repos, pkgs_in=None, quiet=True):
        self._override_sigchecks = False
        self.quiet = quiet
        self.pkgs = pkgs_in or []
        self.repos = repos
        self.pool = solv.Pool()
        self.setup()

    def setPackages(self, pkgs_in):
        self.pkgs = pkgs_in

    def setup(self):
        """
         Load the repos into repostore to query package dependencies
        """
        for repo in self.repos:
            solv_repo = self.pool.add_repo(str(repo['id']))
            solv_path = os.path.join(repo['relative_path'], 'solv')
            if not os.path.isfile(solv_path) or not solv_repo.add_solv(solv.xfopen(str(solv_path)), 0):
                raise Exception("Repository solv file cannot be found at: {}".format(solv_path))
        self.pool.addfileprovides()
        self.pool.createwhatprovides()

    def getDependencylist(self):
        """
         Get dependency list and suggested packages for package names provided.
         The dependency lookup is only one level in this case.
         The package name format could be any of the following:
         name, name.arch, name-ver-rel.arch, name-ver, name-ver-rel,
         epoch:name-ver-rel.arch, name-epoch:ver-rel.arch
        """
        pkgselection = self.pool.Selection()
        flags = solv.Selection.SELECTION_NAME|solv.Selection.SELECTION_PROVIDES|solv.Selection.SELECTION_GLOB
        flags |= solv.Selection.SELECTION_CANON|solv.Selection.SELECTION_DOTARCH|solv.Selection.SELECTION_ADD
        for pkg in self.pkgs:
            pkgselection.select(pkg, flags)
        return self.__locateDeps(pkgselection.solvables())

    def getRecursiveDepList(self):
        """
         Get dependency list and suggested packages for package names provided.
         The dependency lookup is recursive. All available packages in the repo
         are returned matching whatprovides.
         The package name format could be any of the following:
         name, name.arch, name-ver-rel.arch, name-ver, name-ver-rel,
         epoch:name-ver-rel.arch, name-epoch:ver-rel.arch
         returns a dictionary of {'n-v-r.a' : [n,v,e,r,a],...}
        """
        solved = []
        to_solve = self.pkgs
        all_results = {}

        while to_solve:
            log.debug("Solving %s \n\n", to_solve)
            results = self.getDependencylist()
            all_results.update(results)
            found = self.processResults(results)[0]
            solved += to_solve
            to_solve = []
            for _dep, pkgs in list(found.items()):
                for pkg in pkgs:
                    solved = list(set(solved))
                    if str(pkg) not in solved:
                        to_solve.append(str(pkg))
            self.pkgs = to_solve
        return all_results

    def __locateDeps(self, pkgs):
        results = {}

        if not self.quiet:
            print(("Solving Dependencies (%i): " % len(pkgs)))
            pb = ProgressBar(prompt='', endTag=' - complete',
                             finalSize=len(pkgs), finalBarLength=40, stream=sys.stdout)
            pb.printAll(1)

        for pkg in pkgs:
            if not self.quiet:
                pb.addTo(1)
                pb.printIncrement()
            results[pkg] = {}
            reqs = pkg.lookup_deparray(solv.SOLVABLE_REQUIRES)
            pkgresults = results[pkg]
            for req in reqs:
                pkgresults[req] = self.pool.whatprovides(req)
        if not self.quiet:
            pb.printComplete()
        return results

    @staticmethod
    def processResults(results):
        reqlist = {}
        notfound = {}
        for pkg in results:
            if not results[pkg]:
                continue
            for req in results[pkg]:
                rlist = results[pkg][req]
                if not rlist:
                    # Unsatisfied dependency
                    notfound[str(req)] = []
                    continue
                reqlist[str(req)] = rlist
        found = {}
        for req, rlist in list(reqlist.items()):
            found[req] = []
            for r in rlist:
                dep = [r.name, r.evr, r.arch]
                if dep not in found[req]:
                    found[req].append(dep)
        return found, notfound

    @staticmethod
    def printable_result(results):
        print_doc_str = ""
        for pkg in results:
            if not results[pkg]:
                continue
            for req in results[pkg]:
                rlist = results[pkg][req]
                print_doc_str += "\n dependency: %s \n" % req
                if not rlist:
                    # Unsatisfied dependency
                    print_doc_str += "   Unsatisfied dependency \n"
                    continue

                for po in rlist:
                    print_doc_str += "   provider: %s\n" % str(po)
        return print_doc_str


if __name__ == '__main__':
    parser = OptionParser(usage="Usage: %prog [repoid] [repodata_path] [pkgname1] [pkgname2] ... [pkgnameM]")
    parser.add_option("-i", "--input-file", action="store",
        help="YAML file to use as input. This would ignore all other input passed in the command line")
    parser.add_option("-y", "--output-yaml", action="count", help="Produce a YAML formatted output")
    (options, _args) = parser.parse_args()

    arg_repo = []
    arg_pkgs = []

    if options.input_file:
        # Example of YAML input file:
        #
        # repositories:
        #   sles12-sp3-pool-x86_64: /var/cache/rhn/repodata/sles12-sp3-pool-x86_64/
        #   sles12-sp3-updates-x86_64: /var/cache/rhn/repodata/sles12-sp3-updates-x86_64/
        #
        # packages:
        #   - libapr-util1-1.5.3-2.3.1.x86_64
        #   - apache2-utils-2.4.23-29.3.2.x86_64
        #   - python3-base
        #   - apache2-utils
        #
        try:
           repo_cfg = yaml.load(open(options.input_file))
           for repo in repo_cfg['repositories']:
               arg_repo.append({'id': repo, 'relative_path': repo_cfg['repositories'][repo]})
           arg_pkgs = repo_cfg['packages']
        except Exception as exc:
           parser.error("Error reading input file: {}".format(exc))
           sys.exit(1)
    elif len(_args) >= 3:
        arg_repo = [{'id': _args[0],
                    'relative_path': _args[1] }]  # path to where repodata is located
        arg_pkgs = _args[2:]
    else:
        parser.error("Wrong number of arguments")
        sys.exit(1)

    dsolve = DepSolver(arg_repo, arg_pkgs, quiet=options.output_yaml)
    deplist = dsolve.getDependencylist()

    if options.output_yaml:
        output = {
            'packages': [],
            'dependencies' : {}
        }
        for pkg in deplist:
           pkg_tag = str(pkg)
           output['packages'].append(pkg_tag)
           output['dependencies'][pkg_tag] = {}
           for dep in deplist[pkg]:
               output['dependencies'][pkg_tag][str(dep)] = [str(x) for x in deplist[pkg][dep]]
        sys.stdout.write(yaml.dump(output))
    else:
        result_set = dsolve.processResults(deplist)
        print(result_set)
        print("Printable dependency Results: \n\n %s" % dsolve.printable_result(deplist))
