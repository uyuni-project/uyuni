# How to update the mirror directory

## Product information

You need to update the json files inside the mirror directory with the information of new products.
You can use minima-mirror to mirror SCC, provided you have credentials, and then manually copy and paste the product information.
Be careful not to copy the `organitzations_subscriptions.json` file.

Then, use `sed` to replace ?XXXXXX in the urls in those json files.

`sed 's|\(http://[^?]*\)\?.*$|\1|' -i FILE.json`
`sed 's|\(https://[^?]*\)\?.*$|\1|' -i FILE.json`

Replace FILE.json for every json file inside mirror directory.

## Repositories

If you need new repositories, you need to create an empty directory for the ones you need and then run `create_repo`, to generate the new metadata.
Initially you can create the repositories empty.

Then, you need to edit the list of empty repos in `testsuite/features/support/constants.rb`.

## Packages

If you need new packages, create or edit the get_rpms.sh script inside the repository, to download the new package.
Just write the `wget command` you need to use to download the new package.

Then, download the package and call `create_repo`.
 
