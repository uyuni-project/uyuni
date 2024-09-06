# Why are these files here?

We ship config files for VSCode out of the box for two main reasons:  

1. for convenience, so linters, formatters, etc work right out of the box;  
2. to reduce hit-and-run breaking changes in private repos where Github Actions are not available.  

See [this Slack thread](https://suse.slack.com/archives/C02DDMY6R0R/p1724770544871309) for more context.

# How do I manage my own settings?

You can use a workspace file. VSCode allows you to store configuration, launch, etc settings in a workspace file which you can change independently from the directory settings. This is very similar to the regular `.vscode` directory setup.  

There is a helper script provided at `scripts/generate-vscode-workspace.sh` which generates a new workspace file. After doing so, open the workspace file instead of the directory in VSCode and update the settings as needed.  

```
$ ./scripts/generate-vscode-workspace.sh
Enter new workspace name [uyuni]: 
Enter default server hostname for launch configs [server.tf.local]: 
$ open uyuni.code-workspace
```
