# Copyright (c) 2018 SUSE Linux Products GmbH
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
"""
Code for building packages in SUSE that need generated code not tracked in git.
"""
import os

from tito.builder import Builder
from tito.common import  run_command

class SuseGitExtraGenerationBuilder(Builder):

    def _setup_sources(self):

        Builder._setup_sources(self)
        setup_execution_file_name = "setup.sh"
        setup_file_dir = os.path.join(self.git_root, self.relative_project_dir)
        setup_file_path = os.path.join(setup_file_dir, setup_execution_file_name)
        if os.path.exists(setup_file_path):
            output = run_command("[[ -x %s ]] && %s" % (setup_file_path, setup_file_path), True)
            filename = output.split('\n')[-1]
        if filename and os.path.exists(os.path.join(setup_file_dir, filename)):
            run_command("cp %s %s/" % (os.path.join(setup_file_dir, filename), self.rpmbuild_sourcedir))
            self.sources.append(os.path.join(self.rpmbuild_sourcedir, filename))

