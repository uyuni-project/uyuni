import pytest
import os
import re
import io
import bugzilla
from bugzilla.bug import Bug
from changelogs import *

def issues_to_str(issues, num_expected):
    out = f"{len(issues)} issue(s) found ({num_expected} expected)\n"
    out += "\n".join([str(i) for i in issues])
    return out

@pytest.fixture
def tracker_filename(tmp_path):
    p = tmp_path / "trackers.xml"
    p.write_text(r"""
<issue-trackers>
   <issue-tracker>
       <name>tckr</name>
       <regex>tckr#(\d{2})</regex>
   </issue-tracker>
   <issue-tracker>
       <name>bnc</name>
       <regex>(?:bsc|bnc)#(\d+)</regex>
   </issue-tracker>
</issue-trackers>
                 """)
    return str(p)

@pytest.fixture
def file_list():
    return [
        "pkg/path/myfile.txt",
        "pkg/path/mypkg.changes.my.feature",
        "pkg/other/path/file.txt",
        "pkg/other/otherpkg.changes.my.feature"
    ]

@pytest.fixture
def base_path(tmp_path, file_list):
    # Create a temp base dir
    base_path = tmp_path / "base"
    base_path.mkdir()

    # Create files in file_list
    for file in file_list:
        fp = base_path / file
        fp.parent.mkdir(parents=True, exist_ok=True)
        fp.touch()

    # Create rel-eng/packages files
    pkg_dir = base_path / "rel-eng/packages"
    pkg_dir.mkdir(parents=True)

    pkg_file = pkg_dir / "mypkg"
    pkg_file.write_text("1.0.0 pkg/path/")

    pkg_file = pkg_dir / "otherpkg"
    pkg_file.write_text("1.0.0 pkg/other/")
    return base_path

@pytest.fixture
def chlog_file(base_path):
    file = base_path / "pkg/path/mypkg.changes.my.feature"
    return file

@pytest.fixture
def validator(monkeypatch, base_path):
    return ChangelogValidator(base_path, None, None, DEFAULT_LINE_LENGTH, RegexRules(None))

@pytest.fixture
def validator_with_trackers(monkeypatch, tracker_filename, base_path):
    monkeypatch.setenv("BZ_TOKEN", "my-bugzilla-token")
    monkeypatch.setenv("GH_TOKEN", "my-github-token")

    # Mock GitHub API
    def gh_api_call(api_cmd):
        pr_data="""Title of my PR (tckr#99)
First commit message (tckr#99)
Second commit message
"""
        if re.search(r"^gh pr view -R [^ ]+ 999 .*", api_cmd):
            return io.StringIO(pr_data)
        else:
            raise Exception("An error occurred when getting the PR information from the GitHub API.")

    # Mock Bugzilla API
    def getbug(self, bsc):
        if bsc == "1000000":
            return Bug(self, 1000000, {"product": "SUSE Manager 1.0"})
        elif bsc == "2000000":
            return Bug(self, 2000000, {"product": "Not SUSE Manager"})
        elif bsc == "9999999":
            raise xmlrpc.client.Fault(102, "Not authorized")
        else:
            raise xmlrpc.client.Fault(101, "Not found")

    monkeypatch.setattr(os, "popen", gh_api_call)
    monkeypatch.setattr(bugzilla.Bugzilla, "getbug", getbug)
    monkeypatch.setattr(ChangelogValidator, "get_bugzilla_api", lambda self: bugzilla.Bugzilla(url=None))

    return ChangelogValidator(base_path, None, None, DEFAULT_LINE_LENGTH, RegexRules(tracker_filename))

def test_regex_trackers(tracker_filename):
    regex = RegexRules(tracker_filename)
    assert "tckr" in regex.trackers
    assert regex.trackers["tckr"] == r"tckr#(\d{2})"

def test_issue_error_string():
    assert not os.getenv("GITHUB_ACTION")
    issue = Issue(IssueType.EMPTY_LINE, package="mypackage")
    assert str(issue) == f"ERROR: {IssueType.EMPTY_LINE} for package mypackage"

def test_issue_warning_string_all_params():
    assert not os.getenv("GITHUB_ACTION")
    issue = Issue(IssueType.WRONG_SPACING, file="myfile.txt", line=1, package="mypackage", severe=False)
    assert str(issue) == f"WARNING: {IssueType.WRONG_SPACING} for package mypackage in file myfile.txt#L1"

def test_issue_gh_action_string(monkeypatch):
    monkeypatch.setenv("GITHUB_ACTION", "true")
    issue = Issue(IssueType.WRONG_CAP, "myfile.txt", 3, 5)
    assert str(issue) == f"::error file=myfile.txt,line=3,endLine=5::{IssueType.WRONG_CAP} in file myfile.txt#L3-5"

def test_get_pkg_index(validator, file_list):
    pkg_idx = validator.get_pkg_index(file_list)
    assert "mypkg" in pkg_idx
    assert "pkg/path/myfile.txt" in pkg_idx["mypkg"]["files"]
    assert "pkg/path/mypkg.changes.my.feature" in pkg_idx["mypkg"]["changes"]

def test_extract_trackers(validator_with_trackers):
    trackers = validator_with_trackers.extract_trackers("""
        This is a tckr#23 tracker.
        Repeat tckr#23.
        And another tckr#24.
        Not a valid tracker tckr#1,
        Also not a valid tracker tkr#333.
        """)
    assert "tckr" in trackers
    assert len(trackers["tckr"]) == 2
    assert ("tckr#23", "23") in trackers["tckr"]
    assert ("tckr#24", "24") in trackers["tckr"]
    assert ("tckr#1", "1") not in trackers["tckr"]
    assert ("tkr#333", "333") not in trackers["tckr"]

def test_get_entry_obj(validator):
    buffer = ["- This is a changelog entry."]

    entry = validator.get_entry_obj(buffer, "myfile.changes", 5)
    assert entry.entry == "This is a changelog entry."
    assert entry.file == "myfile.changes"
    assert entry.line == 4
    assert not entry.end_line
    assert not entry.trackers

def test_get_entry_obj_multiline(validator):
    buffer = ["- This is a ", "multi line ", "changelog entry."]

    entry = validator.get_entry_obj(buffer, "myfile.changes", 5)
    assert entry.entry == "This is a multi line changelog entry."
    assert entry.file == "myfile.changes"
    assert entry.line == 2
    assert entry.end_line == 4
    assert not entry.trackers

def test_get_entry_obj_with_tracker(validator_with_trackers):
    buffer = ["- This is a changelog entry with a tracker (tckr#99)"]

    entry = validator_with_trackers.get_entry_obj(buffer, "myfile.changes", 1)
    assert ("tckr#99", "99") in entry.trackers["tckr"]

def test_get_entry_obj_with_multiple_trackers(validator_with_trackers):
    buffer = ["- This is a changelog entry with trackers (tckr#01, tckr#02)"]

    entry = validator_with_trackers.get_entry_obj(buffer, "myfile.changes", 2)
    assert entry.entry == "This is a changelog entry with trackers (tckr#01, tckr#02)"
    assert entry.file == "myfile.changes"
    assert entry.line == 1
    assert not entry.end_line
    assert len(entry.trackers["tckr"]) == 2
    assert ("tckr#01", "01") in entry.trackers["tckr"]
    assert ("tckr#02", "02") in entry.trackers["tckr"]

def test_validate_chlog_file_valid(validator, chlog_file):
    chlog_file.write_text("- This is a valid\n  multiline changelog entry\n")
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

def test_validate_chlog_file_multiple_entries(validator, chlog_file):
    chlog_file.write_text("- This is a valid\n  multiline changelog entry\n- This is a second entry\n")
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 2

def test_validate_chlog_file_multiple_entries_line_numbers(validator, chlog_file):
    chlog_file.write_text("- This is a valid\n  multiline changelog entry\n- This is a second entry\n- a multiline\n  entry\n")
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert len(entries) == 3
    issue = issues[0]
    assert IssueType.WRONG_CAP in str(issue)
    assert issue.line == 4
    assert issue.end_line == 5

def test_validate_chlog_file_empty_file(validator, chlog_file):
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert len(issues) == 1
    assert IssueType.EMPTY_CHLOG in str(issues[0])

def test_validate_chlog_file_multiple_issues_and_entries(validator, chlog_file):
    content = """- This is a valid entry
- This is a valid
  multiline entry
- This entry has an extra
  whitespace  at the second line
- Invalid entry with multiple trailing whitespaces at the end  
- Invalid entry: wrong capitalization after the colon
"""
    chlog_file.write_text(content)
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert len(issues) == 3, issues_to_str(issues, 3)
    assert len(entries) == 5

# Tests for the basic rules

@pytest.mark.parametrize("entry_text, issue_msg", [
    ("- This entry has\n  trailing whitespaces \n", IssueType.TRAIL_WHITESPACE),
    ("- This entry has an  extra whitespace\n", IssueType.MULTI_WHITESPACE),
    (" - This is an invalid changelog entry\n", IssueType.WRONG_INDENT),
    ("- This is an invalid changelog entry\n This line has only 1 leading whitespace instead of 2\n", IssueType.WRONG_INDENT),
    ("This changelog entry doesn't start with '- ' characters\n", IssueType.WRONG_START),
    ("- This is an invalid changelog entry without a newline at the end", IssueType.MISSING_NEWLINE),
    ("- This entry\n\n  has an empty line in between\n", IssueType.EMPTY_LINE),
    ("- this entry has wrong capitalization\n", IssueType.WRONG_CAP),
    ("- This entry has wrong capitalization\n  in the. second sentence\n", IssueType.WRONG_CAP),
    ("- This entry has wrong capitalization: right here.\n", IssueType.WRONG_CAP),
    ("- This entry has wrong capitalization.\n  right here.\n", IssueType.WRONG_CAP),
    ("- This entry does not have a space.After a full stop\n", IssueType.WRONG_SPACING),
    ("- This entry does not have a space:After a colon\n", IssueType.WRONG_SPACING),
    ("- This entry is" + " very" * 10 + " long\n", IssueType.LINE_TOO_LONG.format(DEFAULT_LINE_LENGTH)),
])
def test_validate_chlog_file_rules(validator, chlog_file, entry_text, issue_msg):
    chlog_file.write_text(entry_text)
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert issue_msg in str(issues[0])

# Tests for tracker validation rules

def test_validate_trackers(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This entry has a tracker (tckr#99)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator_with_trackers.validate_trackers(entries)
    assert not issues, issues_to_str(issues, 0)

def test_validate_trackers_mistyped(validator, chlog_file):
    chlog_file.write_text("- This entry has a mistyped trackers (ckr#01, yckr#02)\n")
    issues, entries = validator.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator.validate_trackers(entries)
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.MISTYPED_TRACKER in str(issues[0])

def test_validate_trackers_multiple(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This entry has trackers (tckr#01, tckr#02)\n- More trackers (tckr#02, tckr#03)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 2
    assert len(entries[0].trackers["tckr"]) == 2
    assert ("tckr#01", "01") in entries[0].trackers["tckr"]
    assert ("tckr#02", "02") in entries[0].trackers["tckr"]
    assert len(entries[1].trackers["tckr"]) == 2
    assert ("tckr#02", "02") in entries[1].trackers["tckr"]
    assert ("tckr#03", "03") in entries[1].trackers["tckr"]

    issues = validator_with_trackers.validate_trackers(entries)
    assert not issues, issues_to_str(issues, 0)

def test_validate_trackers_with_pr(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This entry has a tracker matching with the PR title (tckr#99)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    issues = validator_with_trackers.validate_trackers(entries)
    assert not issues, issues_to_str(issues, 0)

@pytest.mark.parametrize("entry_text, issue_msg", [
    # In the following cases, tckr#99 is mentioned in the PR title
    ("- This entry doesn't have any trackers\n", IssueType.MISSING_TRACKER.format("tckr#99")),
    ("- This entry has an additional tracker (tckr#99, tckr#00)\n", IssueType.WRONG_TRACKER.format("tckr#00"))
])
def test_validate_trackers_tracker_mismatch(validator_with_trackers, chlog_file, entry_text, issue_msg):
    # Set PR number to validate trackers against the PR
    validator_with_trackers.git_repo = "test/repo"
    validator_with_trackers.pr_number = 999
    chlog_file.write_text(entry_text)
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    issues = validator_with_trackers.validate_trackers(entries)
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert issue_msg in str(issues[0])

def test_validate(validator_with_trackers, base_path, file_list):
    for f in file_list:
        if ".changes." in f:
            p = base_path / f
            p.write_text("- This is a valid changelog entry (tckr#99)\n")

    issues = validator_with_trackers.validate(file_list)
    assert not issues, issues_to_str(issues, 0)

def test_validate_no_changes_in_pkg(validator, chlog_file):
    chlog_file.write_text("- This is a changelog entry.\n")

    issues = validator.validate(["pkg/path/mypkg.changes.my.feature"])
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.WRONG_CHLOG in str(issues[0])

# Tests for changelogs for correct packages

def test_validate_missing_chlog(validator, chlog_file):
    issues = validator.validate(["pkg/path/myfile.txt"])
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.MISSING_CHLOG in str(issues[0])

def test_validate_chlog_for_wrong_pkg(validator, chlog_file):
    chlog_file.write_text("- This is a changelog entry.\n")
    issues = validator.validate(["pkg/path/mypkg.changes.my.feature", "pkg/other/path/file.txt"])
    assert len(issues) == 2, issues_to_str(issues, 2)
    assert any(IssueType.WRONG_CHLOG in str(issue) and "mypkg" in str(issue) for issue in issues)
    assert any(IssueType.MISSING_CHLOG in str(issue) and "otherpkg" in str(issue) for issue in issues)

def test_validate_change_in_subdir(validator, base_path):
    chlog_file = base_path / "pkg/other/otherpkg.changes.my.feature"
    chlog_file.write_text("- This is a changelog entry.\n")
    issues = validator.validate(["pkg/other/otherpkg.changes.my.feature", "pkg/other/path/file.txt"])
    assert not issues, issues_to_str(issues, 0)

# Tests for Bugzilla trackers

def test_validate_bsc(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This is an entry with a valid BZ tracker (bsc#1000000)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator_with_trackers.validate_bsc(entries[0])
    assert not issues, issues_to_str(issues, 0)

def test_validate_bsc_non_existent(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This is an entry with a non-existent BZ tracker (bsc#1234567)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator_with_trackers.validate_bsc(entries[0])
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.BUG_NOT_FOUND.format("1234567") in str(issues[0])

def test_validate_bsc_not_authorized(validator_with_trackers, chlog_file):
    chlog_file.write_text("- This is an entry with a private BZ tracker (bsc#9999999)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator_with_trackers.validate_bsc(entries[0])
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.BUG_NOT_AUTHORIZED.format("9999999") in str(issues[0])

def test_validate_bsc_wrong_product(validator_with_trackers, chlog_file):
    chlog_file.write_text("- An entry with a BZ tracker for another product (bsc#2000000)\n")
    issues, entries = validator_with_trackers.validate_chlog_file(str(chlog_file))
    assert not issues, issues_to_str(issues, 0)
    assert len(entries) == 1

    issues = validator_with_trackers.validate_bsc(entries[0])
    assert len(issues) == 1, issues_to_str(issues, 1)
    assert IssueType.INVALID_PRODUCT.format("2000000") in str(issues[0])
