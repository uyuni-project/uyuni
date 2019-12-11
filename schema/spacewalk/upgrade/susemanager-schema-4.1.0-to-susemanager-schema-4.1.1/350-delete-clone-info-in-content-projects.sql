delete from rhnchannelcloned where id in (select c.id from rhnchannel c inner join susecontentenvironmenttarget et on c.id = et.channel_id);
