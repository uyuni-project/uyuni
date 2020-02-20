
create table
rhnContentSourceSsl
(
	id NUMERIC not null
		constraint rhn_csssl_id_pk primary key,
	content_source_id NUMERIC not null
		constraint rhn_csssl_csid_uq unique
		constraint rhn_csssl_csid_fk references rhnContentSource(id) on delete cascade,
	ssl_ca_cert_id NUMERIC not null
		constraint rhn_csssl_cacertid_fk references rhnCryptoKey(id),
	ssl_client_cert_id NUMERIC
		constraint rhn_csssl_clcertid_fk references rhnCryptoKey(id),
	ssl_client_key_id NUMERIC
		constraint rhn_csssl_clkeyid_fk references rhnCryptoKey(id),
	constraint rhn_csssl_client_chk check(ssl_client_key_id is null or ssl_client_cert_id is not null),
	created TIMESTAMPTZ default(current_timestamp) not null,
	modified TIMESTAMPTZ default(current_timestamp) not null
)

;

create sequence rhn_contentsourcessl_seq;

