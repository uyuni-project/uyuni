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
import shutil

from tito.builder import Builder
from tito.common import  run_command, debug, info_out

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


SPEC_FILE_TEMPLATE = """
Name:           {}
Version:        0
Release:        0
Summary:        Test
License:        Apache-2.0
{}

%description
test

%prep
%autosetup

%build

%install

%files

%changelog
"""


class ContainerBuilder(Builder):
    '''
    Builder class adding the rel-eng/container_push.sh script modifying the Dockerfile when pushing to IBS
    '''
    push_script = "container_push.sh"

    def run(self, options):
        info_out(f"Building package [{self.build_tag}]")
        self.no_cleanup = options.no_cleanup

        # Reset list of artifacts on each call to run().
        self.artifacts = []

        try:
            try:
                self._create_build_dirs()
                self.tgz()
                self.copy_sources()
                self.generate_spec()

                if options.srpm:
                    self.srpm()
            except KeyboardInterrupt:
                print("Interrupted, cleaning up...")
        finally:
            self.cleanup()

        return self.artifacts

    def copy_sources(self):
        '''
        Copy the container files to the rpmbuild source directory for the RPM build process
        '''
        self.copy_push(self.push_script)

        gitdir = os.path.join(self.git_root, self.relative_project_dir)
        for path in os.listdir(gitdir):
            file_path = os.path.join(gitdir, path)
            if os.path.isfile(file_path):
                target_path = os.path.join(self.rpmbuild_sourcedir, path)
                self.copy_source(file_path, target_path)

    def copy_source(self, file_path, target_path):
        debug(f"Copying source {file_path} to {target_path}")
        shutil.copy2(file_path, target_path)
        self.sources.append(target_path)

    def tgz(self):
        # Some builder functions are checking if the sources have been tarball-ed.
        # Just pretend they are since containers sources are not packed.
        self.ran_tgz = True

    def generate_spec(self):
        '''
        Generate a spec file to build the SRPM with
        '''
        debug(f"Generating spec with sources {self.sources}")
        sources = "\n".join([f"SOURCE{idx}: {source}" for idx, source in enumerate(self.sources)])
        spec_content = SPEC_FILE_TEMPLATE.format(self.project_name, sources)

        self.spec_file_name = f"{self.project_name}.spec"
        self.spec_file = os.path.join(self.rpmbuild_sourcedir, self.spec_file_name)
        with open(self.spec_file, "w") as fd:
            fd.write(spec_content)
            

    def copy_push(self, name):
        script_path = os.path.join(self.git_root, "rel-eng", name)
        target_script = os.path.join(self.rpmbuild_sourcedir, "push.sh")
        debug(f"Copying {script_path} to {target_script}")
        shutil.copy2(script_path, target_script)
        self.sources.append(target_script)




class ChartBuilder(ContainerBuilder):
    '''
    Builder class adding the rel-eng/chart_push.sh script modifying the Chart.yaml when pushing to IBS
    '''
    push_script = "chart_push.sh"

    helm_chart_files = ["values.yaml", "values.schema.json", "charts", "crds", "templates", "LICENSE", "README.md"]

    def tgz(self):
        tar_file = os.path.join(self.rpmbuild_sourcedir, f"{self.project_name}.tar")
        gitdir = os.path.join(self.git_root, self.relative_project_dir)
        files = [file for file in self.helm_chart_files if os.path.exists(os.path.join(gitdir, file))]
        run_command(f'tar cf {tar_file} -C {gitdir} {" ".join(files)}')
        self.sources.append(tar_file)

        self.ran_tgz = True

    def copy_source(self, file_path, target_path):
        debug("Checking file " + os.path.basename(file_path))
        if os.path.basename(file_path) not in self.helm_chart_files:
            ContainerBuilder.copy_source(self, file_path, target_path)
