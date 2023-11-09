# Uyuni bash completion

Bash completion scripts for Uyuni CLI tools.

## Manual installation

1. Clone the Git repository into your Uyuni/SUSE Manager server
2. Go to the `susemanager/bash-completion` directory inside the repository
3. Execute `make && make install`

```sh
git clone https://github.com/uyuni-project/uyuni.git
cd uyuni/susemanager/bash-completion
make && make install
```

## Contributing

The completion script for each CLI tool is in a separate file named "tool-name.bash".
Additionally, all the common functions used by the scripts are in `completions/_common.bash`.
`make` combines every script file with `_common.bash` to create a standalone bash completion script.
If you want to create a completion script for a new tool, remember to add your new file as a target in the `Makefile`.
