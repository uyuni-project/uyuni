CREATE TABLE IF NOT EXISTS suseOVALPackageEvrState
(
    id           NUMERIC NOT NULL
        CONSTRAINT suse_oval_pkg_evr_state_id_pk PRIMARY KEY,
    evr          VARCHAR,
    datatype     VARCHAR,
    operation    VARCHAR
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_pkg_evr_state_uq
    ON suseOVALPackageEvrState (evr, datatype, operation)
;

CREATE SEQUENCE IF NOT EXISTS suse_oval_pkg_evr_state_id_seq START WITH 101;

CREATE TABLE IF NOT EXISTS suseOVALPackageArchState
(
    id           NUMERIC NOT NULL
        CONSTRAINT suse_oval_pkg_arch_state_id_pk PRIMARY KEY,
    value        VARCHAR,
    operation    VARCHAR
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_pkg_arch_state_uq
    ON suseOVALPackageArchState (value, operation)
;

CREATE SEQUENCE IF NOT EXISTS suse_oval_pkg_arch_state_id_seq START WITH 1;


CREATE TABLE IF NOT EXISTS suseOVALPackageVersionState
(
    id           NUMERIC NOT NULL
        CONSTRAINT suse_oval_pkg_version_state_id_pk PRIMARY KEY,
    value        VARCHAR,
    operation    VARCHAR
);


CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_pkg_version_state_uq
    ON suseOVALPackageVersionState (value, operation)
;

CREATE SEQUENCE IF NOT EXISTS suse_oval_pkg_version_state_id_seq START WITH 201;

CREATE TABLE IF NOT EXISTS suseOVALDefinition
(
    id              VARCHAR NOT NULL
        CONSTRAINT suse_oval_definition_id_pk PRIMARY KEY,
    class           VARCHAR NOT NULL,
    title           VARCHAR,
    description     VARCHAR(10000),
    cve_id          NUMERIC
        REFERENCES rhnCve(id),
    os_family       VARCHAR,
    os_version      VARCHAR,
    criteria_tree   JSON
);

CREATE TABLE IF NOT EXISTS suseOVALPackageState
(
    id                  VARCHAR NOT NULL
        CONSTRAINT suse_oval_pkg_state_id_pk PRIMARY KEY,
    operator            VARCHAR,
    arch_state_id       NUMERIC
        REFERENCES suseOVALPackageArchState(id),
    version_state_id    NUMERIC
        REFERENCES suseOVALPackageVersionState(id),
    evr_state_id        NUMERIC
        REFERENCES suseOVALPackageEvrState(id),
    isRpm               BOOLEAN
);

CREATE TABLE IF NOT EXISTS suseOVALPackageObject
(
    id         VARCHAR NOT NULL
        CONSTRAINT suse_oval_pkg_object_id_pk PRIMARY KEY,
    name       VARCHAR NOT NULL,
    isRpm      BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS suseOVALPackageTest
(
    id                VARCHAR NOT NULL
        CONSTRAINT suse_oval_pkg_test_id_pk PRIMARY KEY,
    comment           VARCHAR,
    check_exist       VARCHAR,
    test_check        VARCHAR,
    state_operator    VARCHAR,
    isRpm             BOOLEAN,
    pkg_object_id     VARCHAR NOT NULL
        REFERENCES suseOVALPackageObject(id),
    pkg_state_id      VARCHAR
        REFERENCES suseOVALPackageState(id)
);

CREATE TABLE IF NOT EXISTS suseOVALPlatform
(
    id        NUMERIC NOT NULL
        CONSTRAINT suse_oval_platform_id_pk PRIMARY KEY,
    cpe      VARCHAR
);


CREATE SEQUENCE IF NOT EXISTS suse_oval_platform_id_seq START WITH 101;

CREATE TABLE IF NOT EXISTS suseOVALReference
(
    ref_id    VARCHAR NOT NULL,
    definition_id    VARCHAR NOT NULL
        REFERENCES suseOVALDefinition(id),
    source    VARCHAR,
    url    VARCHAR,
    CONSTRAINT suse_oval_reference_id_pk PRIMARY KEY (ref_id, definition_id)
);

CREATE TABLE IF NOT EXISTS suseOVALDefinitionAffectedPlatform
(
    definition_id    VARCHAR NOT NULL
        REFERENCES suseOVALDefinition(id),
    platform_id      NUMERIC
        REFERENCES suseOVALPlatform(id)
);

/*CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_def_affected_plat_uq
    ON suseOVALDefinitionAffectedPlatform(definition_id, platform_id)
;*/

CREATE TABLE IF NOT EXISTS suseOVALVulnerablePackage
(
    id             NUMERIC NOT NULL
        CONSTRAINT suse_oval_vulnerable_pkg_id_pk PRIMARY KEY,
    name           VARCHAR NOT NULL,
    fix_version    VARCHAR
);

CREATE SEQUENCE IF NOT EXISTS suse_oval_vulnerable_pkg_id_seq START WITH 301;


CREATE TABLE IF NOT EXISTS suseOVALPlatformVulnerablePackage
(
    platform_id          NUMERIC NOT NULL
        REFERENCES suseOVALPlatform (id),
    cve_id               NUMERIC NOT NULL
        REFERENCES rhnCve (id),
    vulnerable_pkg_id    NUMERIC
        REFERENCES suseOVALVulnerablePackage (id),
    CONSTRAINT suse_oval_platform_vulnerable_pkg_id_pk PRIMARY KEY (platform_id, cve_id, vulnerable_pkg_id)
);

ALTER TABLE rhnServer
    ADD COLUMN IF NOT EXISTS cpe VARCHAR;