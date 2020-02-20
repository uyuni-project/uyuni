-- oracle equivalent source none

update rhn_notification_formats
set body_format = 'This is a Spacewalk Monitoring Satellite event notification.\n\nTime:      ^[timestamp:"%a %b %d, %H:%M:%S %Z"]\nState:     ^[probe state]\nHost:      ^[hostname] (^[host IP])\nCheck:     ^[probe description]\nMessage:   ^[probe output]\nRun from:  ^[satellite description]\n\nTo acknowledge, reply to this message with this subject line:\n     ACK ^[alert id]\n\nTo immediately escalate, reply to this message with this subject line:\n     NACK ^[alert id]'
where customer_id is null
	and description = 'New Default (2.15)';

update rhn_notification_formats
set body_format = 'This is Spacewalk Monitoring Satellite notification ^[alert id].\n\nTime:      ^[timestamp:"%a %b %d, %H:%M:%S %Z"]\nState:     ^[probe state]\nHost:      ^[hostname] (^[host IP])\nCheck:     ^[probe description]\nMessage:   ^[probe output]\nRun from:  ^[satellite description]',
	reply_format = '\n\nTo acknowledge, reply to this message within ^[ack wait] minutes with this subject line:\n     ACK ^[alert id]\n\nTo immediately escalate, reply to this message within ^[ack wait] minutes with this subject line:\n     NACK ^[alert id]'
where customer_id is null
	and description = 'New Default (2.18)';

