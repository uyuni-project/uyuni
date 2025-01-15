# Changelog validation workflow for GitHub actions

This workflow automates the process of validating changelog entries in Uyuni.
It ensures changelogs are accurate, adhere to formatting standards, and include correct tracker IDs that match PR titles and commit messages.
Additionally, it verifies the existence of referenced bugs in Bugzilla, and provides inline feedback through the GitHub interface.
The validation script can be integrated into CI workflows or run locally for pre-submission checks.

## Features

 - Validation is performed by a standalone script (`.github/workflows/changelogs/changelogs.py`).
 - Checks if the changelog is added to the correct package.
 - Performs basic checks for typos, spacing, capitalization, whitespace, and line length issues.
 - Ensures correct spelling of tracker IDs (e.g., `bsc#`, `jira-#`) mentioned in the changelogs.
 - Matches tracker IDs against the PR title and commit messages.
 - Verifies that a bug exists and belongs to "SUSE Manager" in `bugzilla.suse.com`
 - Pinpoints the exact location of an issue in a changelog file.
 - Recognizes all tracker types as defined by OBS/IBS (see: `.github/workflows/changelogs/trackers.xml`).
 - Provides inline reporting of issues in the GitHub UI.
 - Can be executed locally by executing `changelogs.py` directly (See "How to Run Locally" below)

## How to Run Locally

**Note:** To run the script locally, you need the `gh` (GitHub CLI tool) and the `python-bugzilla` library installed.

### Basic Functionality

Once you finish working on your feature and add the necessary changelog entries, execute the following command from your Uyuni repository root, providing all modified files (including the changelog files) as positional arguments:

```bash
python .github/workflows/changelogs/changelogs.py $(git diff --name-only <pr_base_rev>..<pr_head_ref>)
```
Since no PR or tracker information is provided, PR and tracker validation will be skipped.

### Full validation (PR, trackers, Bugzilla, etc.)

If you have any trackers (e.g., `bsc#xxx`) mentioned in your changelogs, PR title, or commit messages, you can enable additional checks by including the PR number and a tracker file in the command using the corresponding arguments:

```bash
# Get XML tracker definition data from OBS and save it to a file
osc api /issue_trackers > trackers.xml

# Set the required environment variables (for PR and Bugzilla validation)
export GH_TOKEN=<your GitHub access token>
export BZ_TOKEN=<your Bugzilla API key>

python .github/workflows/changelogs/changelogs.py --tracker-file ./trackers.xml --pr-number <your_pr_no> \
    $(git diff --name-only <pr_base_rev>..<pr_head_ref>)
```

If you want to validate a PR in a different fork, specify the Uyuni fork using the `--git-repo` argument.

## More information

For more information, you can run `changelogs.py --help` or refer to the Python docstrings in the code.
