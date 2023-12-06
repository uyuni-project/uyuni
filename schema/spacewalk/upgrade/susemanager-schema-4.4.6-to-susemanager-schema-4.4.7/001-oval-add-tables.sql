CREATE TABLE IF NOT EXISTS suseOVALPlatform
(
    id        NUMERIC NOT NULL
        CONSTRAINT suse_oval_platform_id_pk PRIMARY KEY,
    cpe      VARCHAR
);

CREATE SEQUENCE IF NOT EXISTS suse_oval_platform_id_seq START WITH 101;

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_aff_platform_cpe_uq
    ON suseovalplatform(cpe);

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