-- Table: public.susenotificationmessagetype

-- DROP TABLE public.susenotificationmessagetype;

CREATE TABLE public.susenotificationmessagetype
(
  id numeric NOT NULL,
  label character varying(64) NOT NULL,
  name character varying(64) NOT NULL,
  priority numeric NOT NULL DEFAULT 0,
  created timestamp with time zone NOT NULL DEFAULT now(),
  modified timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT suse_notifmesstype_id_pk PRIMARY KEY (id),
  CONSTRAINT suse_notifmesstype_label_uq UNIQUE (label),
  CONSTRAINT vn_susenotificationmessagetype_label CHECK (label::text <> ''::text),
  CONSTRAINT vn_susenotificationmessagetype_name CHECK (name::text <> ''::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.susenotificationmessagetype
  OWNER TO spacewalk;

-- Index: public.suse_notifmesstype_label_id_idx

-- DROP INDEX public.suse_notifmesstype_label_id_idx;

CREATE INDEX suse_notifmesstype_label_id_idx
  ON public.susenotificationmessagetype
  USING btree
  (label COLLATE pg_catalog."default", id);

CREATE SEQUENCE suse_notifmesstype_id_seq;