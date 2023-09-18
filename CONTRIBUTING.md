# Contributing

## Getting started

The Contributing page in the wiki provides some guidance on how to get started developing Uyuni.

https://github.com/uyuni-project/uyuni/wiki/Contributing

## Pull Requests

The following roles exist in the context of a Pull Request:

- Creator
- Reviewer
- Collaborators

The guidelines noted here describe the expectations that should be met when submitting a Pull request.

### Pull Request Creators

Please follow these steps when creating a new Pull Request:
  - Fill out the Pull Request template.
  - Provide a description for someone not familiar with your code changes to be still able to understand your changes in the context of the project.
  - Enable the reviewer with the provided information in the description to understand the PR without opening any external resources like bug reports.
  - Ensure that before hitting the "Create" button on GitHub you have read and understood the `CONTRIBUTING.md` and linked GitHub Wiki pages.
  - Make sure to introduce appropriate tests for any new functionality. Keep in mind that different parts of the codebase has different test infrastructures (JUnit for Java, Jest for JS, etc).
  - Ensure all automation on GitHub receives a positive result checkmark.
  - State in a comment why a negative result - if occurring - is incorrect or not your fault.
  - Use the GitHub functionality of re-requesting a review in case you haven't gotten a review after a reasonable time.
  - Re-request a review in case you made substantial changes to the Pull Request.
  - Write detailed and meaningful commit messages. Avoid the usage of generic messages like "fix bug". Further information can be found here:
      - https://cbea.ms/git-commit/
      - https://www.freecodecamp.org/news/how-to-write-better-git-commit-messages/

### Pull Request Reviewers

As a Pull Request Reviewer, the most important point is that the Creator is following the points above. In the case that there are things that haven't been followed please kindly ask the Creator to change that. This should also be done for the rest of the contributing guidelines in this document.

Once the points above are fulfilled there are a number of things that should be taken care of:

- Check that the style guides that cannot be automatically enforced, are fulfilled.
- Verify that the description of the Pull Request matches the code that was submitted.
- Check that the submitted changes make sense in the context of the project and branch. A not acceptable example would be a feature backport after the target branch was already declared end of life.
- Verify that the new and modified test cases are useful to the codebase.
- Check that you can understand the code. If you don't understand it the likelihood of a required change is almost given.

Optionally you additionally do the following things as well:

1. If you have a better or different approach to fix the problem, please feel free to point it out. The suggestions shouldn't cause the PR to not be merged.
2. If you have informal comments or nitpicks, please submit them as early as possible and mark them as such accordingly. Informal comments and nitpicks shouldn't cause the PR to not be merged.

Very important during the whole process is that a reviewer should encourage more contributions by the author. Good things should be highlighted and should make the Pull Request Creator feel appraised.

Finally, if your review is completed and all required suggestions have been followed, please provide approval so PR can be merged.

Use these guidelines, but feel free to go beyond the points listed here if you have the capacity.

### Pull Request Collaborators

Please communicate respectfully with each another. So far no extra guidelines have been created for Collaborators.
