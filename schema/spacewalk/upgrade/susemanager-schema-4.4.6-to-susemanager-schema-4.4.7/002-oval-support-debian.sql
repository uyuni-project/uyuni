create or replace function create_definition(id_in varchar, class_in varchar, title_in varchar, descr_in varchar, cve_in varchar,
                                             os_family_in varchar, os_version_in varchar, criteria_tree_in varchar) returns void
    language plpgsql
as
$$
declare
    cve_id_val     numeric;
begin

    /* name has to be unique */
    INSERT INTO rhncve(id, name)
    VALUES (nextval('rhn_cve_id_seq'), cve_in)
    ON CONFLICT(name) DO NOTHING;

    SELECT id INTO cve_id_val FROM rhncve WHERE name = cve_in;

    INSERT INTO suseOVALDefinition(id, class, title, description, cve_id, os_family, os_version, criteria_tree)
    VALUES (id_in, class_in, title_in, descr_in, cve_id_val, os_family_in, os_version_in,
            CAST(criteria_tree_in AS json))
    ON CONFLICT(id) DO UPDATE
        SET id            = EXCLUDED.id,
            class         = EXCLUDED.class,
            title         = EXCLUDED.title,
            description   = EXCLUDED.description,
            cve_id        = EXCLUDED.cve_id,
            os_family     = EXCLUDED.os_family,
            os_version    = EXCLUDED.os_version,
            criteria_tree = EXCLUDED.criteria_tree;

end;
$$;


create or replace procedure add_affected_platform_to_definition(definition_id_in varchar, platform_name_in varchar)
    language plpgsql
as
$$
declare
    platform_id_val   numeric;
begin

    INSERT INTO suseovalplatform(id, cpe)
    VALUES (nextval('suse_oval_platform_id_seq'), platform_name_in)
    ON CONFLICT(cpe) DO NOTHING;

    SELECT id INTO platform_id_val FROM suseovalplatform WHERE cpe = platform_name_in;

    INSERT INTO suseovaldefinitionaffectedplatform(definition_id, platform_id)
    VALUES (definition_id_in, platform_id_val)
    ON CONFLICT ON CONSTRAINT suse_oval_def_affected_plat_uq DO NOTHING;
end;
$$;

create or replace function insert_package_evr_state(evr_in varchar, operation_in varchar, datatype_in varchar) returns numeric
    language plpgsql
as
$$
declare
    evr_state_id   numeric;
begin

    INSERT INTO suseovalpackageevrstate(id, evr, datatype, operation)
    VALUES (nextval('suse_oval_pkg_evr_state_id_seq'), evr_in, datatype_in, operation_in)
    ON CONFLICT(evr, datatype, operation) DO NOTHING;

    SELECT id
    INTO evr_state_id
    FROM suseovalpackageevrstate
    WHERE evr = evr_in
      AND operation = operation_in
      AND datatype = datatype_in;

    return evr_state_id;
end;
$$;


create or replace function insert_package_arch_state(arch_in varchar, operation_in varchar) returns numeric
    language plpgsql
as
$$
declare
    arch_state_id   numeric;
begin

    INSERT INTO suseovalpackagearchstate(id, value, operation)
    VALUES (nextval('suse_oval_pkg_arch_state_id_seq'), arch_in, operation_in)
    ON CONFLICT(value, operation) DO NOTHING;

    SELECT id
    INTO arch_state_id
    FROM suseovalpackagearchstate
    WHERE value = arch_in
      AND operation = operation_in;

    return arch_state_id;
end;
$$;


create or replace function insert_package_version_state(version_in varchar, operation_in varchar) returns numeric
    language plpgsql
as
$$
declare
    version_state_id   numeric;
begin

    INSERT INTO suseovalpackageversionstate(id, value, operation)
    VALUES (nextval('suse_oval_pkg_version_state_id_seq'), version_in, operation_in)
    ON CONFLICT(value, operation) DO NOTHING;

    SELECT id
    INTO version_state_id
    FROM suseovalpackageversionstate
    WHERE value = version_in
      AND operation = operation_in;

    return version_state_id;
end;
$$;

CREATE UNIQUE INDEX IF NOT EXISTS suse_oval_aff_platform_cpe_uq
    ON suseovalplatform(cpe);

CREATE INDEX IF NOT EXISTS suse_oval_reference_definition_id_index
    ON suseOVALReference(definition_id);

CREATE INDEX IF NOT EXISTS suse_oval_definition_aff_platforms_definition_id_index
    ON suseOVALDefinitionAffectedPlatform(definition_id);

ALTER TABLE suseovaldefinitionaffectedplatform
    DROP CONSTRAINT IF EXISTS suse_oval_def_affected_plat_uq;
ALTER TABLE suseovaldefinitionaffectedplatform
    ADD CONSTRAINT suse_oval_def_affected_plat_uq UNIQUE (definition_id, platform_id);