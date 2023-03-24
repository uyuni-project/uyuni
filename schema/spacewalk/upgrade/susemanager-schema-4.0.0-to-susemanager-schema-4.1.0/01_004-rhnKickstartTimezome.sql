insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
               'Africa/Abidjan',
               'Africa/Abidjan',
               (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Abidjan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Accra',
                'Africa/Accra',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Accra'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Addis_Ababa',
                'Africa/Addis_Ababa',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Addis_Ababa'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Algiers',
                'Africa/Algiers',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Algiers'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Asmara',
                'Africa/Asmara',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Asmara'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Bamako',
                'Africa/Bamako',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Bamako'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Bangui',
                'Africa/Bangui',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Bangui'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Banjul',
                'Africa/Banjul',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Banjul'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Bissau',
                'Africa/Bissau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Bissau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Blantyre',
                'Africa/Blantyre',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Blantyre'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Brazzaville',
                'Africa/Brazzaville',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Brazzaville'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Bujumbura',
                'Africa/Bujumbura',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Bujumbura'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Cairo',
                'Africa/Cairo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Cairo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Casablanca',
                'Africa/Casablanca',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Casablanca'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Ceuta',
                'Africa/Ceuta',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Ceuta'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Conakry',
                'Africa/Conakry',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Conakry'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Dakar',
                'Africa/Dakar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Dakar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Dar_es_Salaam',
                'Africa/Dar_es_Salaam',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Dar_es_Salaam'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Djibouti',
                'Africa/Djibouti',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Djibouti'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Douala',
                'Africa/Douala',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Douala'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/El_Aaiun',
                'Africa/El_Aaiun',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/El_Aaiun'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Freetown',
                'Africa/Freetown',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Freetown'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Gaborone',
                'Africa/Gaborone',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Gaborone'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Harare',
                'Africa/Harare',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Harare'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Johannesburg',
                'Africa/Johannesburg',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Johannesburg'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Juba',
                'Africa/Juba',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Juba'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Kampala',
                'Africa/Kampala',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Kampala'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Khartoum',
                'Africa/Khartoum',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Khartoum'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Kigali',
                'Africa/Kigali',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Kigali'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Kinshasa',
                'Africa/Kinshasa',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Kinshasa'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Lagos',
                'Africa/Lagos',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Lagos'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Libreville',
                'Africa/Libreville',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Libreville'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Lome',
                'Africa/Lome',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Lome'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Luanda',
                'Africa/Luanda',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Luanda'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Lubumbashi',
                'Africa/Lubumbashi',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Lubumbashi'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Lusaka',
                'Africa/Lusaka',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Lusaka'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Malabo',
                'Africa/Malabo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Malabo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Maputo',
                'Africa/Maputo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Maputo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Maseru',
                'Africa/Maseru',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Maseru'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Mbabane',
                'Africa/Mbabane',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Mbabane'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Mogadishu',
                'Africa/Mogadishu',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Mogadishu'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Monrovia',
                'Africa/Monrovia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Monrovia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Nairobi',
                'Africa/Nairobi',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Nairobi'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Ndjamena',
                'Africa/Ndjamena',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Ndjamena'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Niamey',
                'Africa/Niamey',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Niamey'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Nouakchott',
                'Africa/Nouakchott',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Nouakchott'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Ouagadougou',
                'Africa/Ouagadougou',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Ouagadougou'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Porto-Novo',
                'Africa/Porto-Novo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Porto-Novo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Sao_Tome',
                'Africa/Sao_Tome',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Sao_Tome'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Tripoli',
                'Africa/Tripoli',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Tripoli'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Tunis',
                'Africa/Tunis',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Tunis'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Africa/Windhoek',
                'Africa/Windhoek',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Africa/Windhoek'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Adak',
                'America/Adak',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Adak'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Anchorage',
                'America/Anchorage',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Anchorage'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Anguilla',
                'America/Anguilla',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Anguilla'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Antigua',
                'America/Antigua',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Antigua'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Araguaina',
                'America/Araguaina',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Araguaina'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Buenos_Aires',
                'America/Argentina/Buenos_Aires',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Buenos_Aires'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Catamarca',
                'America/Argentina/Catamarca',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Catamarca'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Cordoba',
                'America/Argentina/Cordoba',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Cordoba'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Jujuy',
                'America/Argentina/Jujuy',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Jujuy'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/La_Rioja',
                'America/Argentina/La_Rioja',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/La_Rioja'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Mendoza',
                'America/Argentina/Mendoza',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Mendoza'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Rio_Gallegos',
                'America/Argentina/Rio_Gallegos',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Rio_Gallegos'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Salta',
                'America/Argentina/Salta',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Salta'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/San_Juan',
                'America/Argentina/San_Juan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/San_Juan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/San_Luis',
                'America/Argentina/San_Luis',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/San_Luis'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Tucuman',
                'America/Argentina/Tucuman',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Tucuman'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Argentina/Ushuaia',
                'America/Argentina/Ushuaia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Argentina/Ushuaia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Aruba',
                'America/Aruba',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Aruba'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Asuncion',
                'America/Asuncion',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Asuncion'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Atikokan',
                'America/Atikokan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Atikokan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Bahia',
                'America/Bahia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Bahia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Bahia_Banderas',
                'America/Bahia_Banderas',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Bahia_Banderas'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Barbados',
                'America/Barbados',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Barbados'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Belem',
                'America/Belem',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Belem'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Belize',
                'America/Belize',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Belize'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Blanc-Sablon',
                'America/Blanc-Sablon',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Blanc-Sablon'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Boa_Vista',
                'America/Boa_Vista',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Boa_Vista'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Bogota',
                'America/Bogota',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Bogota'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Boise',
                'America/Boise',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Boise'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Cambridge_Bay',
                'America/Cambridge_Bay',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Cambridge_Bay'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Campo_Grande',
                'America/Campo_Grande',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Campo_Grande'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Cancun',
                'America/Cancun',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Cancun'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Caracas',
                'America/Caracas',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Caracas'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Cayenne',
                'America/Cayenne',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Cayenne'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Cayman',
                'America/Cayman',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Cayman'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Chicago',
                'America/Chicago',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Chicago'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Chihuahua',
                'America/Chihuahua',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Chihuahua'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Costa_Rica',
                'America/Costa_Rica',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Costa_Rica'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Creston',
                'America/Creston',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Creston'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Cuiaba',
                'America/Cuiaba',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Cuiaba'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Curacao',
                'America/Curacao',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Curacao'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Danmarkshavn',
                'America/Danmarkshavn',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Danmarkshavn'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Dawson',
                'America/Dawson',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Dawson'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Dawson_Creek',
                'America/Dawson_Creek',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Dawson_Creek'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Denver',
                'America/Denver',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Denver'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Detroit',
                'America/Detroit',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Detroit'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Dominica',
                'America/Dominica',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Dominica'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Edmonton',
                'America/Edmonton',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Edmonton'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Eirunepe',
                'America/Eirunepe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Eirunepe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/El_Salvador',
                'America/El_Salvador',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/El_Salvador'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Fortaleza',
                'America/Fortaleza',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Fortaleza'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Glace_Bay',
                'America/Glace_Bay',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Glace_Bay'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Godthab',
                'America/Godthab',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Godthab'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Goose_Bay',
                'America/Goose_Bay',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Goose_Bay'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Grand_Turk',
                'America/Grand_Turk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Grand_Turk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Grenada',
                'America/Grenada',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Grenada'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Guadeloupe',
                'America/Guadeloupe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Guadeloupe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Guatemala',
                'America/Guatemala',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Guatemala'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Guayaquil',
                'America/Guayaquil',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Guayaquil'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Guyana',
                'America/Guyana',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Guyana'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Halifax',
                'America/Halifax',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Halifax'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Havana',
                'America/Havana',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Havana'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Hermosillo',
                'America/Hermosillo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Hermosillo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Indianapolis',
                'America/Indiana/Indianapolis',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Indianapolis'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Knox',
                'America/Indiana/Knox',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Knox'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Marengo',
                'America/Indiana/Marengo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Marengo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Petersburg',
                'America/Indiana/Petersburg',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Petersburg'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Tell_City',
                'America/Indiana/Tell_City',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Tell_City'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Vevay',
                'America/Indiana/Vevay',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Vevay'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Vincennes',
                'America/Indiana/Vincennes',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Vincennes'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Indiana/Winamac',
                'America/Indiana/Winamac',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Indiana/Winamac'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Inuvik',
                'America/Inuvik',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Inuvik'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Iqaluit',
                'America/Iqaluit',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Iqaluit'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Jamaica',
                'America/Jamaica',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Jamaica'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Juneau',
                'America/Juneau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Juneau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Kentucky/Louisville',
                'America/Kentucky/Louisville',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Kentucky/Louisville'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Kentucky/Monticello',
                'America/Kentucky/Monticello',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Kentucky/Monticello'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Kralendijk',
                'America/Kralendijk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Kralendijk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/La_Paz',
                'America/La_Paz',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/La_Paz'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Lima',
                'America/Lima',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Lima'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Los_Angeles',
                'America/Los_Angeles',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Los_Angeles'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Lower_Princes',
                'America/Lower_Princes',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Lower_Princes'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Maceio',
                'America/Maceio',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Maceio'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Managua',
                'America/Managua',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Managua'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Manaus',
                'America/Manaus',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Manaus'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Marigot',
                'America/Marigot',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Marigot'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Martinique',
                'America/Martinique',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Martinique'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Matamoros',
                'America/Matamoros',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Matamoros'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Mazatlan',
                'America/Mazatlan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Mazatlan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Menominee',
                'America/Menominee',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Menominee'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Merida',
                'America/Merida',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Merida'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Metlakatla',
                'America/Metlakatla',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Metlakatla'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Mexico_City',
                'America/Mexico_City',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Mexico_City'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Miquelon',
                'America/Miquelon',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Miquelon'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Moncton',
                'America/Moncton',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Moncton'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Monterrey',
                'America/Monterrey',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Monterrey'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Montevideo',
                'America/Montevideo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Montevideo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Montreal',
                'America/Montreal',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Montreal'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Montserrat',
                'America/Montserrat',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Montserrat'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Nassau',
                'America/Nassau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Nassau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/New_York',
                'America/New_York',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/New_York'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Nipigon',
                'America/Nipigon',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Nipigon'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Nome',
                'America/Nome',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Nome'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Noronha',
                'America/Noronha',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Noronha'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/North_Dakota/Beulah',
                'America/North_Dakota/Beulah',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/North_Dakota/Beulah'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/North_Dakota/Center',
                'America/North_Dakota/Center',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/North_Dakota/Center'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/North_Dakota/New_Salem',
                'America/North_Dakota/New_Salem',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/North_Dakota/New_Salem'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Ojinaga',
                'America/Ojinaga',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Ojinaga'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Panama',
                'America/Panama',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Panama'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Pangnirtung',
                'America/Pangnirtung',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Pangnirtung'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Paramaribo',
                'America/Paramaribo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Paramaribo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Phoenix',
                'America/Phoenix',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Phoenix'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Port_of_Spain',
                'America/Port_of_Spain',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Port_of_Spain'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Port-au-Prince',
                'America/Port-au-Prince',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Port-au-Prince'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Porto_Velho',
                'America/Porto_Velho',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Porto_Velho'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Puerto_Rico',
                'America/Puerto_Rico',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Puerto_Rico'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Rainy_River',
                'America/Rainy_River',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Rainy_River'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Rankin_Inlet',
                'America/Rankin_Inlet',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Rankin_Inlet'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Recife',
                'America/Recife',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Recife'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Regina',
                'America/Regina',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Regina'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Resolute',
                'America/Resolute',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Resolute'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Rio_Branco',
                'America/Rio_Branco',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Rio_Branco'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Santa_Isabel',
                'America/Santa_Isabel',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Santa_Isabel'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Santarem',
                'America/Santarem',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Santarem'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Santiago',
                'America/Santiago',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Santiago'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Santo_Domingo',
                'America/Santo_Domingo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Santo_Domingo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Sao_Paulo',
                'America/Sao_Paulo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Sao_Paulo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Scoresbysund',
                'America/Scoresbysund',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Scoresbysund'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Shiprock',
                'America/Shiprock',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Shiprock'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Sitka',
                'America/Sitka',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Sitka'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Barthelemy',
                'America/St_Barthelemy',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Barthelemy'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Johns',
                'America/St_Johns',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Johns'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Kitts',
                'America/St_Kitts',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Kitts'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Lucia',
                'America/St_Lucia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Lucia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Thomas',
                'America/St_Thomas',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Thomas'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/St_Vincent',
                'America/St_Vincent',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/St_Vincent'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Swift_Current',
                'America/Swift_Current',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Swift_Current'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Tegucigalpa',
                'America/Tegucigalpa',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Tegucigalpa'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Thule',
                'America/Thule',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Thule'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Thunder_Bay',
                'America/Thunder_Bay',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Thunder_Bay'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Tijuana',
                'America/Tijuana',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Tijuana'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Toronto',
                'America/Toronto',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Toronto'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Tortola',
                'America/Tortola',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Tortola'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Vancouver',
                'America/Vancouver',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Vancouver'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Whitehorse',
                'America/Whitehorse',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Whitehorse'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Winnipeg',
                'America/Winnipeg',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Winnipeg'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Yakutat',
                'America/Yakutat',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Yakutat'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'America/Yellowknife',
                'America/Yellowknife',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'America/Yellowknife'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Casey',
                'Antarctica/Casey',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Casey'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Davis',
                'Antarctica/Davis',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Davis'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/DumontDUrville',
                'Antarctica/DumontDUrville',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/DumontDUrville'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Macquarie',
                'Antarctica/Macquarie',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Macquarie'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Mawson',
                'Antarctica/Mawson',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Mawson'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/McMurdo',
                'Antarctica/McMurdo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/McMurdo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Palmer',
                'Antarctica/Palmer',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Palmer'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Rothera',
                'Antarctica/Rothera',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Rothera'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/South_Pole',
                'Antarctica/South_Pole',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/South_Pole'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Syowa',
                'Antarctica/Syowa',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Syowa'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Antarctica/Vostok',
                'Antarctica/Vostok',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Antarctica/Vostok'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Arctic/Longyearbyen',
                'Arctic/Longyearbyen',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Arctic/Longyearbyen'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Aden',
                'Asia/Aden',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Aden'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Almaty',
                'Asia/Almaty',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Almaty'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Amman',
                'Asia/Amman',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Amman'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Anadyr',
                'Asia/Anadyr',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Anadyr'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Aqtau',
                'Asia/Aqtau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Aqtau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Aqtobe',
                'Asia/Aqtobe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Aqtobe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Ashgabat',
                'Asia/Ashgabat',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Ashgabat'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Baghdad',
                'Asia/Baghdad',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Baghdad'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Bahrain',
                'Asia/Bahrain',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Bahrain'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Baku',
                'Asia/Baku',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Baku'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Bangkok',
                'Asia/Bangkok',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Bangkok'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Beirut',
                'Asia/Beirut',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Beirut'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Bishkek',
                'Asia/Bishkek',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Bishkek'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Brunei',
                'Asia/Brunei',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Brunei'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Choibalsan',
                'Asia/Choibalsan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Choibalsan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Chongqing',
                'Asia/Chongqing',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Chongqing'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Colombo',
                'Asia/Colombo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Colombo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Damascus',
                'Asia/Damascus',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Damascus'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Dhaka',
                'Asia/Dhaka',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Dhaka'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Dili',
                'Asia/Dili',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Dili'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Dubai',
                'Asia/Dubai',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Dubai'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Dushanbe',
                'Asia/Dushanbe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Dushanbe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Gaza',
                'Asia/Gaza',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Gaza'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Harbin',
                'Asia/Harbin',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Harbin'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Hebron',
                'Asia/Hebron',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Hebron'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Ho_Chi_Minh',
                'Asia/Ho_Chi_Minh',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Ho_Chi_Minh'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Hong_Kong',
                'Asia/Hong_Kong',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Hong_Kong'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Hovd',
                'Asia/Hovd',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Hovd'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Irkutsk',
                'Asia/Irkutsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Irkutsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Jakarta',
                'Asia/Jakarta',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Jakarta'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Jayapura',
                'Asia/Jayapura',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Jayapura'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Jerusalem',
                'Asia/Jerusalem',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Jerusalem'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kabul',
                'Asia/Kabul',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kabul'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kamchatka',
                'Asia/Kamchatka',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kamchatka'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Karachi',
                'Asia/Karachi',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Karachi'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kashgar',
                'Asia/Kashgar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kashgar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kathmandu',
                'Asia/Kathmandu',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kathmandu'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kolkata',
                'Asia/Kolkata',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kolkata'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Krasnoyarsk',
                'Asia/Krasnoyarsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Krasnoyarsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kuala_Lumpur',
                'Asia/Kuala_Lumpur',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kuala_Lumpur'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kuching',
                'Asia/Kuching',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kuching'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Kuwait',
                'Asia/Kuwait',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Kuwait'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Macau',
                'Asia/Macau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Macau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Magadan',
                'Asia/Magadan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Magadan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Makassar',
                'Asia/Makassar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Makassar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Manila',
                'Asia/Manila',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Manila'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Muscat',
                'Asia/Muscat',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Muscat'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Nicosia',
                'Asia/Nicosia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Nicosia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Novokuznetsk',
                'Asia/Novokuznetsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Novokuznetsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Novosibirsk',
                'Asia/Novosibirsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Novosibirsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Omsk',
                'Asia/Omsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Omsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Oral',
                'Asia/Oral',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Oral'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Phnom_Penh',
                'Asia/Phnom_Penh',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Phnom_Penh'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Pontianak',
                'Asia/Pontianak',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Pontianak'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Pyongyang',
                'Asia/Pyongyang',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Pyongyang'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Qatar',
                'Asia/Qatar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Qatar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Qyzylorda',
                'Asia/Qyzylorda',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Qyzylorda'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Rangoon',
                'Asia/Rangoon',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Rangoon'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Riyadh',
                'Asia/Riyadh',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Riyadh'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Sakhalin',
                'Asia/Sakhalin',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Sakhalin'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Samarkand',
                'Asia/Samarkand',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Samarkand'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Seoul',
                'Asia/Seoul',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Seoul'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Shanghai',
                'Asia/Shanghai',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Shanghai'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Singapore',
                'Asia/Singapore',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Singapore'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Taipei',
                'Asia/Taipei',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Taipei'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Tashkent',
                'Asia/Tashkent',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Tashkent'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Tbilisi',
                'Asia/Tbilisi',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Tbilisi'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Tehran',
                'Asia/Tehran',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Tehran'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Thimphu',
                'Asia/Thimphu',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Thimphu'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Tokyo',
                'Asia/Tokyo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Tokyo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Ulaanbaatar',
                'Asia/Ulaanbaatar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Ulaanbaatar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Urumqi',
                'Asia/Urumqi',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Urumqi'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Vientiane',
                'Asia/Vientiane',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Vientiane'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Vladivostok',
                'Asia/Vladivostok',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Vladivostok'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Yakutsk',
                'Asia/Yakutsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Yakutsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Yekaterinburg',
                'Asia/Yekaterinburg',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Yekaterinburg'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Asia/Yerevan',
                'Asia/Yerevan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Asia/Yerevan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Azores',
                'Atlantic/Azores',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Azores'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Bermuda',
                'Atlantic/Bermuda',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Bermuda'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Canary',
                'Atlantic/Canary',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Canary'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Cape_Verde',
                'Atlantic/Cape_Verde',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Cape_Verde'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Faroe',
                'Atlantic/Faroe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Faroe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Madeira',
                'Atlantic/Madeira',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Madeira'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Reykjavik',
                'Atlantic/Reykjavik',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Reykjavik'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/South_Georgia',
                'Atlantic/South_Georgia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/South_Georgia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/St_Helena',
                'Atlantic/St_Helena',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/St_Helena'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Atlantic/Stanley',
                'Atlantic/Stanley',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Atlantic/Stanley'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Adelaide',
                'Australia/Adelaide',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Adelaide'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Brisbane',
                'Australia/Brisbane',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Brisbane'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Broken_Hill',
                'Australia/Broken_Hill',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Broken_Hill'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Currie',
                'Australia/Currie',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Currie'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Darwin',
                'Australia/Darwin',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Darwin'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Eucla',
                'Australia/Eucla',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Eucla'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Hobart',
                'Australia/Hobart',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Hobart'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Lindeman',
                'Australia/Lindeman',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Lindeman'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Lord_Howe',
                'Australia/Lord_Howe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Lord_Howe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Melbourne',
                'Australia/Melbourne',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Melbourne'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Perth',
                'Australia/Perth',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Perth'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Australia/Sydney',
                'Australia/Sydney',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Australia/Sydney'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Amsterdam',
                'Europe/Amsterdam',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Amsterdam'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Andorra',
                'Europe/Andorra',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Andorra'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Athens',
                'Europe/Athens',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Athens'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Belgrade',
                'Europe/Belgrade',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Belgrade'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Berlin',
                'Europe/Berlin',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Berlin'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Bratislava',
                'Europe/Bratislava',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Bratislava'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Brussels',
                'Europe/Brussels',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Brussels'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Bucharest',
                'Europe/Bucharest',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Bucharest'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Budapest',
                'Europe/Budapest',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Budapest'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Chisinau',
                'Europe/Chisinau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Chisinau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Copenhagen',
                'Europe/Copenhagen',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Copenhagen'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Dublin',
                'Europe/Dublin',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Dublin'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Gibraltar',
                'Europe/Gibraltar',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Gibraltar'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Guernsey',
                'Europe/Guernsey',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Guernsey'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Helsinki',
                'Europe/Helsinki',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Helsinki'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Isle_of_Man',
                'Europe/Isle_of_Man',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Isle_of_Man'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Istanbul',
                'Europe/Istanbul',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Istanbul'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Jersey',
                'Europe/Jersey',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Jersey'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Kaliningrad',
                'Europe/Kaliningrad',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Kaliningrad'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Kiev',
                'Europe/Kiev',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Kiev'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Lisbon',
                'Europe/Lisbon',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Lisbon'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Ljubljana',
                'Europe/Ljubljana',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Ljubljana'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/London',
                'Europe/London',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/London'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Luxembourg',
                'Europe/Luxembourg',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Luxembourg'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Madrid',
                'Europe/Madrid',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Madrid'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Malta',
                'Europe/Malta',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Malta'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Mariehamn',
                'Europe/Mariehamn',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Mariehamn'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Minsk',
                'Europe/Minsk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Minsk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Monaco',
                'Europe/Monaco',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Monaco'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Moscow',
                'Europe/Moscow',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Moscow'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Oslo',
                'Europe/Oslo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Oslo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Paris',
                'Europe/Paris',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Paris'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Podgorica',
                'Europe/Podgorica',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Podgorica'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Prague',
                'Europe/Prague',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Prague'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Riga',
                'Europe/Riga',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Riga'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Rome',
                'Europe/Rome',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Rome'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Samara',
                'Europe/Samara',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Samara'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/San_Marino',
                'Europe/San_Marino',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/San_Marino'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Sarajevo',
                'Europe/Sarajevo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Sarajevo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Simferopol',
                'Europe/Simferopol',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Simferopol'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Skopje',
                'Europe/Skopje',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Skopje'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Sofia',
                'Europe/Sofia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Sofia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Stockholm',
                'Europe/Stockholm',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Stockholm'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Tallinn',
                'Europe/Tallinn',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Tallinn'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Tirane',
                'Europe/Tirane',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Tirane'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Uzhgorod',
                'Europe/Uzhgorod',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Uzhgorod'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Vaduz',
                'Europe/Vaduz',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Vaduz'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Vatican',
                'Europe/Vatican',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Vatican'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Vienna',
                'Europe/Vienna',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Vienna'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Vilnius',
                'Europe/Vilnius',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Vilnius'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Volgograd',
                'Europe/Volgograd',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Volgograd'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Warsaw',
                'Europe/Warsaw',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Warsaw'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Zagreb',
                'Europe/Zagreb',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Zagreb'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Zaporozhye',
                'Europe/Zaporozhye',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Zaporozhye'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Europe/Zurich',
                'Europe/Zurich',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Europe/Zurich'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Antananarivo',
                'Indian/Antananarivo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Antananarivo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Chagos',
                'Indian/Chagos',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Chagos'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Christmas',
                'Indian/Christmas',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Christmas'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Cocos',
                'Indian/Cocos',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Cocos'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Comoro',
                'Indian/Comoro',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Comoro'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Kerguelen',
                'Indian/Kerguelen',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Kerguelen'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Mahe',
                'Indian/Mahe',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Mahe'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Maldives',
                'Indian/Maldives',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Maldives'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Mauritius',
                'Indian/Mauritius',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Mauritius'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Mayotte',
                'Indian/Mayotte',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Mayotte'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Indian/Reunion',
                'Indian/Reunion',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Indian/Reunion'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Apia',
                'Pacific/Apia',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Apia'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Auckland',
                'Pacific/Auckland',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Auckland'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Chatham',
                'Pacific/Chatham',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Chatham'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Chuuk',
                'Pacific/Chuuk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Chuuk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Easter',
                'Pacific/Easter',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Easter'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Efate',
                'Pacific/Efate',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Efate'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Enderbury',
                'Pacific/Enderbury',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Enderbury'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Fakaofo',
                'Pacific/Fakaofo',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Fakaofo'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Fiji',
                'Pacific/Fiji',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Fiji'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Funafuti',
                'Pacific/Funafuti',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Funafuti'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Galapagos',
                'Pacific/Galapagos',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Galapagos'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Gambier',
                'Pacific/Gambier',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Gambier'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Guadalcanal',
                'Pacific/Guadalcanal',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Guadalcanal'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Guam',
                'Pacific/Guam',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Guam'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Honolulu',
                'Pacific/Honolulu',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Honolulu'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Johnston',
                'Pacific/Johnston',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Johnston'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Kiritimati',
                'Pacific/Kiritimati',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Kiritimati'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Kosrae',
                'Pacific/Kosrae',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Kosrae'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Kwajalein',
                'Pacific/Kwajalein',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Kwajalein'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Majuro',
                'Pacific/Majuro',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Majuro'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Marquesas',
                'Pacific/Marquesas',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Marquesas'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Midway',
                'Pacific/Midway',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Midway'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Nauru',
                'Pacific/Nauru',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Nauru'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Niue',
                'Pacific/Niue',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Niue'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Norfolk',
                'Pacific/Norfolk',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Norfolk'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Noumea',
                'Pacific/Noumea',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Noumea'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Pago_Pago',
                'Pacific/Pago_Pago',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Pago_Pago'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Palau',
                'Pacific/Palau',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Palau'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Pitcairn',
                'Pacific/Pitcairn',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Pitcairn'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Pohnpei',
                'Pacific/Pohnpei',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Pohnpei'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Port_Moresby',
                'Pacific/Port_Moresby',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Port_Moresby'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Rarotonga',
                'Pacific/Rarotonga',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Rarotonga'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Saipan',
                'Pacific/Saipan',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Saipan'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Tahiti',
                'Pacific/Tahiti',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Tahiti'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Tarawa',
                'Pacific/Tarawa',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Tarawa'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Tongatapu',
                'Pacific/Tongatapu',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Tongatapu'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Wake',
                'Pacific/Wake',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Wake'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

insert into rhnKickstartTimezone (id, label, name, install_type)
        select sequence_nextval('rhn_ks_timezone_id_seq'),
                'Pacific/Wallis',
                'Pacific/Wallis',
                (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8')
        from dual
        where not exists (select 1 from rhnKickstartTimezone
                           where label = 'Pacific/Wallis'
                             and install_type = (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label = 'rhel_8'));

