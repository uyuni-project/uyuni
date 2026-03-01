CREATE TABLE IF NOT EXISTS suseOVALPlatform
(
    id        NUMERIC NOT NULL
        CONSTRAINT suse_oval_platform_id_pk PRIMARY KEY,
    cpe      VARCHAR NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS suse_oval_platform_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_aff_platform_cpe_uq
    ON suseovalplatform(cpe);

CREATE TABLE IF NOT EXISTS suseOVALVulnerablePackage
(
    id             NUMERIC NOT NULL
        CONSTRAINT suse_oval_vulnerable_pkg_id_pk PRIMARY KEY,
    name           VARCHAR NOT NULL,
    fix_version    VARCHAR
);

CREATE SEQUENCE IF NOT EXISTS suse_oval_vulnerable_pkg_id_seq;

CREATE INDEX IF NOT EXISTS suse_oval_vulnerable_pkg_name_idx ON suseOVALVulnerablePackage(name);

DO $$
DECLARE
    pkg_id_type text;
BEGIN
    SELECT data_type
      INTO pkg_id_type
      FROM information_schema.columns
     WHERE table_name = 'suseovalvulnerablepackage'
       AND column_name = 'id';

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'suseovalplatformvulnerablepackage'
    ) THEN
        IF pkg_id_type = 'numeric' THEN
                CREATE TABLE suseOVALPlatformVulnerablePackage
                (
                    platform_id       NUMERIC NOT NULL
                        REFERENCES suseOVALPlatform (id),
                    cve_id            NUMERIC NOT NULL
                        REFERENCES rhnCve (id),
                    vulnerable_pkg_id NUMERIC NOT NULL
                        REFERENCES suseOVALVulnerablePackage (id),
                    CONSTRAINT suse_oval_platform_vulnerable_pkg_id_pk
                        PRIMARY KEY (platform_id, cve_id, vulnerable_pkg_id)
                );
        ELSIF pkg_id_type = 'bigint' THEN
                CREATE TABLE suseOVALPlatformVulnerablePackage
                (
                    platform_id       NUMERIC NOT NULL
                        REFERENCES suseOVALPlatform (id),
                    cve_id            NUMERIC NOT NULL
                        REFERENCES rhnCve (id),
                    vulnerable_pkg_id BIGINT NOT NULL,
                    CONSTRAINT suse_oval_platform_vulnerable_pkg_id_pk
                        PRIMARY KEY (platform_id, cve_id, vulnerable_pkg_id)
                );
        END IF;
    END IF;
END;
$$;

ALTER TABLE rhnServer
    ADD COLUMN IF NOT EXISTS cpe VARCHAR(64);
