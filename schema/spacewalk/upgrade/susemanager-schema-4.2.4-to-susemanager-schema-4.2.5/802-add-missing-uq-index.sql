
CREATE UNIQUE INDEX IF NOT EXISTS suseproductchannel_product_id_channel_id_uq
ON suseProductChannel (product_id, channel_id);

CREATE UNIQUE INDEX IF NOT EXISTS susemdkeyword_label_uq
ON suseMdKeyword (label);

CREATE UNIQUE INDEX IF NOT EXISTS suseupgradepath_from_pdid_to_pdid_uq
ON suseUpgradePath (from_pdid, to_pdid);
