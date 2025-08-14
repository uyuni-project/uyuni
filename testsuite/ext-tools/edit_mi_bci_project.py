#!/usr/bin/python3

# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import argparse
import subprocess
import os
import xml.etree.ElementTree as ET
import tempfile
import re

def run_osc_command(command, input_data=None):
    """
    A helper function to run osc commands and handle their output and errors.
    It returns the stdout of the command.
    """
    try:
        # Use subprocess.PIPE to capture stdout and stderr
        result = subprocess.run(command, input=input_data, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"Error running OSC command: {' '.join(command)}")
        print(f"Stdout: {e.stdout}")
        print(f"Stderr: {e.stderr}")
        exit(1)

def main():
    """
    Main function to parse arguments and run the update logic.
    """
    parser = argparse.ArgumentParser(description="Automate editing OBS project metadata and configuration.")
    parser.add_argument("--project", required=True, help="The name of the OBS project (e.g., home:oscar-barrios:SLE_Testing).")
    parser.add_argument("--apiurl", default="https://api.suse.de", help="The OBS API URL to use (e.g., https://api.suse.de).")
    parser.add_argument("--prefer-package", help="The value for the 'Prefer' rule in project config (e.g., container:suse-manager-5.0-init-5.0.5).")
    parser.add_argument("--mi-project", help="The project name of the MI to validate (e.g., SUSE:Maintenance:12345).")
    parser.add_argument("--mi-repo-name", help="The repository name including the packages to validate (e.g., SUSE_Updates_SLE-Module-Basesystem_15-SP6_x86_64).")
    args = parser.parse_args()

    project_name = args.project
    api_url = args.apiurl

    # --- Edit Project Configuration (prjconf) ---
    if args.prefer_package:
        print(f"Editing project configuration for '{project_name}'...")

        # 1. Get current project configuration using `osc meta prjconf`
        prjconf_cmd = ["osc", "-A", api_url, "meta", "prjconf", project_name]
        current_prjconf = run_osc_command(prjconf_cmd)

        # 2. Modify prjconf content to set the 'Prefer' rule
        prefer_line = f"Prefer: {args.prefer_package}"
        lines = current_prjconf.splitlines()
        new_lines = []
        found_prefer = False
        for line in lines:
            if line.strip().startswith("Prefer:"):
                # Replace the existing line
                new_lines.append(prefer_line)
                found_prefer = True
            else:
                new_lines.append(line)
        if not found_prefer:
            # Add the line if it doesn't exist
            new_lines.append(prefer_line)

        modified_prjconf = "\n".join(new_lines) + "\n"

        # 3. Write modified prjconf to a temporary file and upload it
        with tempfile.NamedTemporaryFile(mode='w', delete=False) as tmp_file:
            tmp_file.write(modified_prjconf)
            tmp_file_path = tmp_file.name

        upload_cmd = ["osc", "-A", api_url, "meta", "prjconf", project_name, "-F", tmp_file_path]
        run_osc_command(upload_cmd)
        os.remove(tmp_file_path)
        print("Project configuration updated successfully.")

    # --- Edit Project Metadata (meta) ---
    if args.repo_project and args.repo_name:
        print(f"Editing project metadata for '{project_name}'...")

        # 1. Get current project metadata using `osc meta prj`
        meta_cmd = ["osc", "-A", api_url, "meta", "prj", project_name]
        current_meta_xml = run_osc_command(meta_cmd)

        # 2. Parse and modify the XML using ElementTree
        try:
            root = ET.fromstring(current_meta_xml)
            containerfile_repo = root.find(".//repository[@name='containerfile']")
            if containerfile_repo is None:
                raise ValueError("Could not find repository with name='containerfile' in the metadata.")

            # Find the path to modify by its original project name
            path_to_modify = None
            for path_elem in containerfile_repo.findall("./path"):
                # Use a regular expression to match projects like "SUSE:Maintenance:12345"
                if re.match(r'SUSE:Maintenance:\d+', path_elem.get('project')):
                    path_to_modify = path_elem
                    break

            if path_to_modify is None:
                raise ValueError("Could not find a matching SUSE:Maintenance path to modify in the metadata.")

            # Update the 'project' and 'repository' attributes
            path_to_modify.set('project', args.repo_project)
            path_to_modify.set('repository', args.repo_name)

            # 3. Write modified XML to a temporary file and upload it
            modified_meta_xml = ET.tostring(root, encoding='unicode')

            with tempfile.NamedTemporaryFile(mode='w', delete=False) as tmp_file:
                tmp_file.write(modified_meta_xml)
                tmp_file_path = tmp_file.name

            upload_cmd = ["osc", "-A", api_url, "meta", "prj", project_name, "-F", tmp_file_path]
            run_osc_command(upload_cmd)
            os.remove(tmp_file_path)
            print("Project metadata updated successfully.")

        except ET.ParseError as e:
            print(f"Error parsing XML: {e}")
            exit(1)
        except ValueError as e:
            print(f"Error: {e}")
            exit(1)

if __name__ == "__main__":
    main()
