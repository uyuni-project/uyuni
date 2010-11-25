create table
suseProductChannel
(
    product_id number        not null
                             CONSTRAINT spc_pid_fk
                             REFERENCES suseProducts (id),
    channel_id number        not null
                             CONSTRAINT spc_rhn_cid_fk
                             REFERENCES rhnchannel (id),

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

