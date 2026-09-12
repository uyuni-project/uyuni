Place new schema migration scripts for the upcoming, unreleased schema
version in this directory during development (numbered like any other
upgrade directory, e.g. `100-my-change.sql`).

`spacewalk-schema-upgrade` applies the scripts found here automatically,
after the version-to-version migrations, regardless of whether the schema
version was bumped yet.

At release time, `tito` moves the content of this directory into the
proper `<old-version>-to-<new-version>` directory and tags the release.
Do not create versioned upgrade directories by hand.
