Place new schema migration scripts for the upcoming, unreleased schema
version in this directory during development (numbered like any other
upgrade directory, e.g. `100-my-change.sql`).

`spacewalk-schema-upgrade --reportdb` applies the scripts found here
automatically, after the version-to-version migrations, regardless of
whether the schema version was bumped yet. It does that on every run,
so scripts in here must be safe to apply more than once. They keep
being re-applied on every server start until a release moves them out.

At release time, `tito` moves the content of this directory into the
proper `<old-version>-to-<new-version>` directory and tags the release.
Do not create versioned upgrade directories by hand.
