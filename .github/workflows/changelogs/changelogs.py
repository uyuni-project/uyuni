#!/usr/bin/env python3

import logging
import argparse
import sys
import os
import re
import linecache
import functools
import bugzilla
import xmlrpc
import requests
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field

DEFAULT_LINE_LENGTH = 67
DEFAULT_GIT_REPO = "uyuni-project/uyuni"
DEFAULT_BUGZILLA_URI = "bugzilla.suse.com"

class RegexRules:
    """Contains the regex rules for all the changelog checks

    Can additionally contain dynamic regex rules that are acquired from a
    'tracker file'. This is an XML file that contains definitions of all the
    tracker IDs that OBS/IBS supports. The contents of the file can be
    retrieved from the '/issue-trackers' endpoint of the IBS/OBS APIs.
    """

    MULTIW = re.compile(r"\S[ \t]{2,}[^ ]")
    TRAILINGW = re.compile(r"[ \t]$")
    WRONG_CAP_START = re.compile(r"^\W*[a-z]")
    WRONG_CAP_AFTER = re.compile(r"[:.] *[a-z]")
    WRONG_SPACING = re.compile(r"([.,;:])[^ \n]")
    TRACKER_LIKE = re.compile(r".{2,5}#\d+")

    def __init__(self, tracker_filename: str = None):
        trackers = {}
        if tracker_filename:
            try:
                logging.info(f"Parsing tracker file: {tracker_filename}")
                tree = ET.parse(tracker_filename)
            except FileNotFoundError as e:
                raise Exception(f"{e.strerror}: '{e.filename}'")
            except ET.ParseError as e:
                raise Exception(f"Error parsing '{tracker_filename}': {e.msg}")

            for tracker in tree.getroot():
                try:
                    # Every <issue-tracker> element should
                    # contain 'name' and 'regex' as children
                    name = tracker.find('name').text
                    regex = tracker.find('regex').text
                except AttributeError:
                    raise Exception(f"Error parsing '{tracker_filename}': not a tracker XML file")
                trackers[name] = regex

            logging.info(f"Found {len(trackers.keys())} tracker definition(s)")

        self.trackers = trackers

class IssueType:
    """Contains the issue messages as static strings"""

    LINE_TOO_LONG = "Line exceeds {} characters"
    EMPTY_LINE = "Empty line"
    WRONG_INDENT = "Wrong indentation"
    WRONG_START = "Entries must start with '- ' characters"
    MULTI_WHITESPACE = "Multiple whitespaces"
    TRAIL_WHITESPACE = "Trailing whitespaces"
    MISSING_CHLOG = "Changelog not added"
    WRONG_CHLOG = "Changelog added without changes"
    EMPTY_CHLOG = "No changelog entries found"
    MISSING_NEWLINE = "Missing newline at the end"
    WRONG_CAP = "Wrong capitalization"
    WRONG_SPACING = "Wrong spacing"
    WRONG_TRACKER = "{} is not mentioned in PR title or in commit messages"
    MISSING_TRACKER = "{} is not mentioned in any changelog entries"
    MISTYPED_TRACKER = "Possibly a mistyped tracker"
    BUG_NOT_FOUND = "Bug #{} does not exist at Bugzilla"
    BUG_NOT_AUTHORIZED = "Not authorized to access bug #{} at Bugzilla"
    INVALID_BUG = "Some error occurred when accessing bug #{} at Bugzilla: {}"
    INVALID_PRODUCT = "Bug #{} does not belong to SUSE Manager"

@dataclass
class Entry:
    """Class that represents a single changelog entry

    Each Entry contains the entry text, its file, the beginning and ending
    lines, and any extracted tracker information mentioned in the entry
    text.

    The ending line is 'None' if the entry consists of a single line.
    """

    entry: str
    file: str
    line: int
    end_line: int = None
    trackers: dict = field(default_factory=dict)

@dataclass
class Issue:
    """Class that represents a single validation issue

    Each Issue contains a message describing the issue, the file and the
    package the issue is found in, the beginning and ending lines in the
    file, and a 'severe' flag that denotes whether the issues is severe
    (causing a validation failure) or not.

    The ending line is 'None' if the entry consists of a single line.

    The Issue class overrides the '__str__' method to pretty-print itself.
    The actual print format depends on the environment this program runs in.
    When run as a GitHub action (GITHUB_ACTION environment variable is set),
    special GitHub workflow commands are prepended to the messages. This
    lets GitHub UI to display the issue messages in a richer way.

    See: https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions
    """

    msg: str
    file: str = None
    line: int = None
    end_line: int = None
    package: str = None
    severe: bool = True
    # TODO: Pinpoint the column number for issues where applicable

    def get_message_header(self) -> str:
        """Return a message prefix for the issue

        The prefix depends on the issue type, the environment, and the
        additional information available.
        """

        # Prepend special workflow tags if running as a GitHub action
        if os.getenv("GITHUB_ACTION"):
            msg = "::error" if self.severe else "::warning"
            if self.file:
                msg += " file={}".format(self.file)
                if self.line:
                    msg += ",line={}".format(self.line)
                    if self.end_line:
                        msg += ",endLine={}".format(self.end_line)

            return msg + "::"
        else:
            return "ERROR: " if self.severe else "WARNING: "

    def __str__(self):
        out = self.get_message_header()
        out += self.msg

        if self.package:
            out += " for package " + self.package
        if self.file:
            out += " in file " + self.file
            if self.line:
                out += "#L" + str(self.line)
                if self.end_line:
                    out += "-" + str(self.end_line)
        return out

class ChangelogValidator:
    """Class that handles the changelog validation

    Validates the changelog entries by enforcing various rules, such as;
      - Basic typos, spacing, capitalization and line length issues
      - Ensuring the changelogs are added for correct packages

    Additionally, the following checks are made if a tracker file is
    provided;
      - Correct spelling of tracker IDs defined by IBS/OBS
      - Correct matching of tracker IDs between the PR title, comment
        messages and the changelog entries (only if a valid PR number is
        provided)
      - For Bugzilla trackers, the bug exists in the specified Bugzilla
        host
      - For Bugzilla trackers, the bug is reported for the "SUSE Manager"
        product (passes with a warning if the check fails)

    If a tracker file and a PR number is provided, the following environment
    variables must be also set:

        GH_TOKEN: A GitHub access token with the basic privileges
        BZ_TOKEN: A valid Bugzilla API key

    The trackers XML file (trackers.xml):
        This is an XML document that contains definitions and formats of all
        the various types of tracker IDs recognized by IBS/OBS. The document is
        provided by the OBS/IBS APIs.

        It can be obtained by sending a request to the '/issue-trackers'
        endpoint of the OBS/IBS API:

        `osc api /issue-trackers`
    """

    def __init__(self, spacewalk_root: str, git_repo: str, pr_number: int, max_line_length: int,
                 regex_rules: type[RegexRules]):
        if pr_number and not os.getenv("GH_TOKEN"):
            raise Exception("GitHub API key not set. Please set it in 'GH_TOKEN' environment variable.")

        self.spacewalk_root = spacewalk_root
        self.git_repo = git_repo
        self.pr_number = pr_number
        self.max_line_length = max_line_length
        self.regex = regex_rules
        if regex_rules.trackers:
            self.bzapi = self.get_bugzilla_api()

    def get_bugzilla_api(self) -> type[bugzilla.Bugzilla]:
        """Initialize and authenticate the Bugzilla API"""

        api_key = os.getenv("BZ_TOKEN")
        if not api_key:
            raise Exception("Bugzilla API key not set. Please set it in 'BZ_TOKEN' environment variable.")

        uri = os.getenv("BUGZILLA_URI", DEFAULT_BUGZILLA_URI)
        try:
            logging.info(f"Initializing Bugzilla API at '{uri}'")
            bzapi = bugzilla.Bugzilla(uri, api_key=api_key)
        except requests.exceptions.ConnectionError as e:
            raise ConnectionError(f"Cannot connect to the Bugzilla API at '{uri}'")

        try:
            assert bzapi.logged_in, f"Cannot log into the Bugzilla API at '{uri}'"
        except xmlrpc.client.Fault as f:
            raise Exception(f"Cannot log in to the Bugzilla API at '{uri}': {f.faultString}")


        return bzapi

    def get_modified_files_for_pkg(self, pkg_path: str, pkg_name: str, files: list[str]) -> dict[str, list[str]]:
        """Return a dictionary of modified files in a package

        The files lists are split into 2 different groups in the dictionary:

          'changes': The changelog files (packagename.changes.*)
          'files': The rest of the modified files in the package
        """

        pkg_files = []
        pkg_chlogs = []
        for f in files:
            # Check if the file exists in a subdirectory of the base path of the package
            if os.path.normpath(os.path.dirname(f)).startswith(os.path.normpath(pkg_path)):
                if os.path.basename(f).startswith(pkg_name + ".changes."):
                    # Ignore if the change is a removal
                    if os.path.isfile(os.path.join(self.spacewalk_root, f)):
                        pkg_chlogs.append(f)
                else:
                    pkg_files.append(f)

        return { "files": pkg_files, "changes": pkg_chlogs }

    def get_pkg_index(self, files: list[str]) -> dict[str, list[str]]:
        """Index the list of modified files

        Parses the list of files and returns a dictionary in the following
        format:

          ["pkg_name"] -> {
              "files": list of changed files in the package,
              "changes": list of modified changelog files in the package
          }

        The actual package names and their base paths are read from the files in
        the 'rel-eng/packages' directory. Each file in this directory defines
        the name of a package (the name of the file), the current version of the
        package, and its base path.
        """

        packages_dir = os.path.join(self.spacewalk_root, "rel-eng/packages")
        pkg_idx = {}

        try:
            pkg_names = os.listdir(packages_dir)
            logging.debug(f"Found {len(pkg_names)} package(s) in 'rel-eng/packages'")
        except FileNotFoundError:
            raise Exception(f"Not an Uyuni repository. Consider using '--spacewalk-dir' option.")

        for pkg_name in pkg_names:
            if pkg_name.startswith('.'):
                # Skip hidden files in rel-eng/packages
                continue
            # Extract the package path from the file:
            # Each file contains the package version and the
            # package path, separated by a space character
            pkg_path = linecache.getline(os.path.join(packages_dir, pkg_name), 1).rstrip().split(maxsplit=1)[1]
            logging.debug(f"Package {pkg_name} is in path {pkg_path}")

            # Get the list of modified files and changelog files for the package
            modified_files = self.get_modified_files_for_pkg(pkg_path, pkg_name, files)
            if modified_files["files"] or modified_files["changes"]:
                pkg_idx[pkg_name] = modified_files

        return pkg_idx

    def extract_trackers(self, text: str) -> dict[str, list[tuple[str, str]]]:
        """Extract all the mentioned trackers in a body of text

        The trackers to be extracted are defined in the 'regex' object.

        The method returns a dictionary of trackers in the following format:

            ["kind"] -> [(full_tracker_id, num_tracker_id)]

            Example of a tuple: ("bsc#1234567", "1234567")
        """

        trackers = {}
        for kind, regex in self.regex.trackers.items():
            trackers_of_kind = []
            for match in re.finditer(regex, text):
                # Match groups are defined by contract in the tracker file
                # Group 1 should be the numeric ID of the tracker
                trackers_of_kind.append(match.group(0, 1))

            # Gather the unique trackers
            trackers[kind] = list(set(trackers_of_kind))

        return trackers

    def get_pr_trackers(self, git_repo: str, pr_number: int) -> dict[str, list[tuple[str, str]]]:
        """Get all the trackers mentioned in a PR

        The trackers are extracted from the PR title and the commit messages.
        """

        assert pr_number
        assert git_repo

        logging.info(f"Requesting information for PR#{pr_number} at '{git_repo}'")
        stream = os.popen(f'gh pr view -R {git_repo} {pr_number} --json title,commits -q ".title, .commits[].messageHeadline, .commits[].messageBody | select(length > 0)"')
        commits = stream.read()
        if stream.close():
            raise Exception("An error occurred when getting the PR information from the GitHub API.")
        return self.extract_trackers(commits)

    def validate_chlog_entry(self, entry: type[Entry]) -> list[type[Issue]]:
        """Validate a single changelog entry"""

        issues = []
        # Test capitalization
        if re.search(self.regex.WRONG_CAP_START, entry.entry) or re.search(self.regex.WRONG_CAP_AFTER, entry.entry):
            issues.append(Issue(IssueType.WRONG_CAP, entry.file, entry.line, entry.end_line))
        # Test spacing
        if re.search(self.regex.WRONG_SPACING, entry.entry):
            issues.append(Issue(IssueType.WRONG_SPACING, entry.file, entry.line, entry.end_line))

        return issues

    def get_entry_obj(self, buffer: list[str], file: str, line_no: int) -> type[Entry]:
        """Create an Entry object from a buffer of entry lines

        The elements in the 'buffer' list are separate lines of a single entry.
        """

        # Strip the '- ' characters in the beginning of the first line
        msg = ''.join(buffer)[2:]
        trackers = self.extract_trackers(msg)
        return Entry(msg, file, line_no - len(buffer), line_no - 1 if len(buffer) > 1 else None, trackers)

    def validate_chlog_file(self, file: str) -> tuple[list[type[Issue]], list[type[Entry]]]:
        """Validate a single changelog file"""

        logging.debug(f"Validating changelog file: {file}")
        file_path = os.path.join(self.spacewalk_root, file)

        if os.path.getsize(file_path) == 0:
            return ([Issue(IssueType.EMPTY_CHLOG, file)], [])

        f = open(file_path, "r")
        issues = []
        entries = []
        entry_buf: list[str] = [] # List to buffer the lines in a single changelog entry
        line_no = 0

        for line in f:
            line_no += 1

            if not line.endswith("\n"):
                issues.append(Issue(IssueType.MISSING_NEWLINE, file))

            stripped_line = line.rstrip("\n")
            if not stripped_line.strip():
                issues.append(Issue(IssueType.EMPTY_LINE, file, line_no))
                continue

            if re.search(r"^- ", stripped_line):
                # Start of a new entry
                if entry_buf:
                    # Wrap up the previous entry
                    entry = self.get_entry_obj(entry_buf, file, line_no)
                    issues.extend(self.validate_chlog_entry(entry))
                    entries.append(entry)
                    entry_buf = [stripped_line]
                else:
                    # First entry in the file
                    entry_buf.append(stripped_line)
            else:
                # Successive lines of the entry
                if entry_buf:
                    if not re.search(r"^  [^ ]", stripped_line):
                        # Successive lines must be indented by two spaces
                        issues.append(Issue(IssueType.WRONG_INDENT, file, line_no))
                    # Strip 1 whitespace from the left
                    # 2 (indentation) - 1 (a literal space after the last line)
                    entry_buf.append(stripped_line[1:])
                elif re.search(r"^\s+- ", stripped_line):
                    # No space allowed before the first entry line
                    issues.append(Issue(IssueType.WRONG_INDENT, file, line_no))
                else:
                    # All entries must start with '- ' characters
                    issues.append(Issue(IssueType.WRONG_START, file, line_no))

            if len(stripped_line) > self.max_line_length:
                issues.append(Issue(IssueType.LINE_TOO_LONG.format(self.max_line_length), file, line_no))
            if re.search(self.regex.MULTIW, stripped_line):
                issues.append(Issue(IssueType.MULTI_WHITESPACE, file, line_no))
            if re.search(self.regex.TRAILINGW, stripped_line):
                issues.append(Issue(IssueType.TRAIL_WHITESPACE, file, line_no))

        # EOF
        if entry_buf:
            # Validate and append the last entry
            entry = self.get_entry_obj(entry_buf, file, line_no + 1)
            issues.extend(self.validate_chlog_entry(entry))
            entries.append(entry)

        return (issues, entries)

    def validate_bsc(self, entry: type[Entry]) -> list[type[Issue]]:
        """Validate Bugzilla trackers against a Bugzilla host"""

        issues = []
        # 'bnc' is the name of the tracker as defined in the trackers file
        if 'bnc' in entry.trackers:
            for tracker, bug_id in entry.trackers['bnc']:
                try:
                    bug = self.bzapi.getbug(bug_id)
                    logging.debug(f"Bug #{bug_id} belongs to product '{bug.product}'")

                    if not bug.product.startswith("SUSE Manager"):
                        issues.append(Issue(IssueType.INVALID_PRODUCT.format(bug_id), entry.file, entry.line, \
                                            entry.end_line, severe=False))
                except xmlrpc.client.Fault as f:
                    if f.faultCode == 101:
                        # Bug not found
                        issues.append(Issue(IssueType.BUG_NOT_FOUND.format(bug_id), entry.file, entry.line, \
                                            entry.end_line, severe=True))
                    elif f.faultCode == 102:
                        # Not authorized
                        issues.append(Issue(IssueType.BUG_NOT_AUTHORIZED.format(bug_id), entry.file, entry.line, \
                                            entry.end_line, severe=False))
                    else:
                        # Any other fault
                        issues.append(Issue(IssueType.INVALID_BUG.format(bug_id, f.faultString), entry.file, entry.line, \
                                            entry.end_line, severe=False))
        return issues


    def validate_trackers(self, entries: list[type[Entry]]) -> list[type[Issue]]:
        """Validate the trackers mentioned in a list of entries

        Checks any possible typos and verifies Bugzilla trackers via the
        Bugzilla API.

        If a PR number is provided, additionally match the mentioned trackers
        against the PR's title and comment messages.
        """

        issues = []

        if self.pr_number and self.git_repo:
            pr_validation = True
            pr_trackers = self.get_pr_trackers(self.git_repo, self.pr_number)
        else:
            pr_validation = False

        all_trackers = {}

        for entry in entries:
            # Check for mistyped trackers
            # Count actual trackers in the entry
            num_trackers = functools.reduce(lambda x, y: x + len(y), entry.trackers.values(), 0)
            # Find all tracker-like words
            if len(re.findall(self.regex.TRACKER_LIKE, entry.entry)) > num_trackers:
                issues.append(Issue(IssueType.MISTYPED_TRACKER, entry.file, entry.line, entry.end_line, severe=False))

            for kind, trackers in entry.trackers.items():
                # Collect all trackers in all entries of the changelog
                if kind not in all_trackers:
                    all_trackers[kind] = entry.trackers[kind]
                else:
                    all_trackers[kind].extend(entry.trackers[kind])

                # Check if all the trackers mentioned in the
                # changelog entry are also mentioned in the PR
                if pr_validation:
                    for t in trackers:
                        if kind not in pr_trackers or t not in pr_trackers[kind]:
                            # Tracker not mentioned in the PR
                            issues.append(Issue(IssueType.WRONG_TRACKER.format(t[0]), entry.file, entry.line, \
                                                entry.end_line))

            # Check Bugzilla trackers via the API
            issues.extend(self.validate_bsc(entry))

        # Check if all the trackers mentioned in the
        # PR are also mentioned in the changelogs
        if pr_validation:
            for kind, trackers in pr_trackers.items():
                for t in trackers:
                    if kind not in all_trackers or t not in all_trackers[kind]:
                        issues.append(Issue(IssueType.MISSING_TRACKER.format(t[0])))

        return issues

    def validate(self, file_list: list[str]) -> list[type[Issue]]:
        """Validates changelogs in the list of files"""

        # Index the list of files by package
        self.pkg_idx = self.get_pkg_index(file_list)
        issues = []
        entries = []
        for pkg, files in self.pkg_idx.items():
            # General checks (package/changelog file mismatch)
            if not files["files"]:
                # Changelog added but no file is modified
                issues.append(Issue(IssueType.WRONG_CHLOG, package=pkg))
            if not files["changes"]:
                # Files are modified but no changelog file added
                issues.append(Issue(IssueType.MISSING_CHLOG, package=pkg))

            # Validate each changelog file and gather all the issues
            for file in files["changes"]:
                i, e = self.validate_chlog_file(file)
                issues.extend(i)
                entries.extend(e)

        # Validate all the mentioned trackers if the tracker file is provided
        if self.regex.trackers:
            issues.extend(self.validate_trackers(entries))

        return issues

def parse_args():
    parser = argparse.ArgumentParser(description="Validate changelog entries for Uyuni PRs",
                                     epilog="Uyuni project: <https://github.com/uyuni-project/uyuni>")

    parser.add_argument("-v", "--verbose",
                        action="store_true",
                        help="enable verbose output")

    parser.add_argument("-l", "--line-length",
                        type=int,
                        default=DEFAULT_LINE_LENGTH,
                        help="maximum line length allowed in changelog files (default: 67)")

    parser.add_argument("-t", "--tracker-file",
                        help="tracker definitions XML document retrieved from the OBS/IBS API. Bypass tracker validation if not provided.")

    parser.add_argument("-d", "--spacewalk-dir",
                        default=".",
                        help="path to the local git repository root (default: current directory)")

    parser.add_argument("-p", "--pr-number",
                        type=int,
                        help="the ID of the pull request to be validated. Bypass PR validation if not provided.")

    parser.add_argument("-r", "--git-repo",
                        default=DEFAULT_GIT_REPO,
                        help=f"the Uyuni repository to validate the PR against (default: '{DEFAULT_GIT_REPO}')")

    parser.add_argument("-b", "--bugzilla-uri",
                        default=DEFAULT_BUGZILLA_URI,
                        help=f"the URI to the Bugzilla host to verify bug trackers (default: '{DEFAULT_BUGZILLA_URI}')")

    parser.add_argument("files",
                        metavar="FILE",
                        nargs="*",
                        help="the list of modified files in the pull request")
    return parser.parse_args()

def init_logging(verbose):
    if verbose:
        log_level = logging.DEBUG
    else:
        log_level = logging.INFO

    logging.basicConfig(level=log_level, format="%(levelname)s: %(message)s")

def main():
    args = parse_args()
    init_logging(args.verbose)

    is_gh_action = os.getenv("GITHUB_ACTION")
    if is_gh_action:
        logging.info("Running in GitHub actions environment")

    try:
        logging.debug("Initializing the validator")
        regexRules = RegexRules(args.tracker_file)
        validator = ChangelogValidator(args.spacewalk_dir, args.git_repo, args.pr_number, args.line_length, regexRules)

        logging.debug(f"Validating {len(args.files)} file(s)")
        issues = validator.validate(args.files)
        logging.debug(f"Validation finished with {len(issues)} issue(s)")
    except Exception as e:
        print(e, file=sys.stderr)
        return 2

    is_fail = any([issue.severe for issue in issues])

    if not issues:
        logging.info("Changelog test passed")
        return 0

    logging.info("Changelog test {} with {} issue(s):".format("failed" if is_fail else "passed", len(issues)))
    if not is_gh_action:
        print("-" * 60)
    for i in issues: print(i)

    if is_fail:
        print()
        print("{}See https://github.com/uyuni-project/uyuni/wiki/Contributing for a guide to writing changelogs."
          .format("::notice::" if is_gh_action else ""))
        return 1

    return 0

if __name__ == '__main__':
    sys.exit(main())
