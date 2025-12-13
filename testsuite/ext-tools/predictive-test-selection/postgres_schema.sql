CREATE TABLE "test_runs"(
    "id" INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "pr_number" INTEGER NOT NULL,
    "run_number" SMALLINT NOT NULL,
    "github_id" BIGINT NOT NULL,
    "commit_sha" TEXT NOT NULL,
    "passed" BOOLEAN NOT NULL,
    "executed_at" TIMESTAMP(0) WITH
        TIME zone NOT NULL
);

ALTER TABLE
    "test_runs" ADD CONSTRAINT "test_runs_github_id_unique" UNIQUE("github_id");

COMMENT ON COLUMN "test_runs"."pr_number" IS 'PR number that triggered this run';
COMMENT ON COLUMN "test_runs"."run_number" IS 'The sequential number of this test run within the same pull request (1 for the first run, 2 for the second, etc.)';
COMMENT ON COLUMN test_runs.github_id IS 'GitHub run ID (unique per GitHub run)';
COMMENT ON COLUMN test_runs.commit_sha IS 'Commit SHA that triggered this run';


CREATE TABLE "feature_results"(
    "run_id" INTEGER NOT NULL,
    "feature_id" SMALLINT NOT NULL,
    "passed" BOOLEAN NOT NULL
);

ALTER TABLE
    "feature_results" ADD PRIMARY KEY("run_id", "feature_id");


CREATE TABLE "files"(
    "id" INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "path" TEXT NOT NULL
);

ALTER TABLE
    "files" ADD CONSTRAINT "files_path_unique" UNIQUE("path");


CREATE TABLE "run_modified_files"(
    "run_id" INTEGER NOT NULL,
    "file_id" INTEGER NOT NULL
);

ALTER TABLE
    "run_modified_files" ADD PRIMARY KEY("run_id", "file_id");


CREATE TABLE "feature_categories"(
    "id" SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "name" TEXT NOT NULL
);

ALTER TABLE
    "feature_categories" ADD CONSTRAINT "feature_categories_name_unique" UNIQUE("name");


CREATE TABLE "features"(
    "id" SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "name" TEXT NOT NULL,
    "category_id" SMALLINT NOT NULL,
    "scenario_count" SMALLINT NOT NULL
);

COMMENT ON COLUMN features.scenario_count IS 'Number of Cucumber scenarios in the feature';

ALTER TABLE
    "features" ADD CONSTRAINT "features_name_unique" UNIQUE("name");


ALTER TABLE
    "run_modified_files" ADD CONSTRAINT "run_modified_files_run_id_foreign" FOREIGN KEY("run_id") REFERENCES "test_runs"("id");
ALTER TABLE
    "features" ADD CONSTRAINT "features_category_id_foreign" FOREIGN KEY("category_id") REFERENCES "feature_categories"("id");
ALTER TABLE
    "feature_results" ADD CONSTRAINT "feature_results_run_id_foreign" FOREIGN KEY("run_id") REFERENCES "test_runs"("id");
ALTER TABLE
    "feature_results" ADD CONSTRAINT "feature_results_feature_id_foreign" FOREIGN KEY("feature_id") REFERENCES "features"("id") ON DELETE CASCADE;
ALTER TABLE
    "run_modified_files" ADD CONSTRAINT "run_modified_files_file_id_foreign" FOREIGN KEY("file_id") REFERENCES "files"("id") ON DELETE CASCADE;