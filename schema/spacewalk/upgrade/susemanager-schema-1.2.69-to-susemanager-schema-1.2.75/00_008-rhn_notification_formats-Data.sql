--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

update rhn_notification_formats set body_format = 'This is a Monitoring event notification.\n\nTime:      ^[timestamp:"%a %b %d, %H:%M:%S %Z"]\nState:     ^[probe state]\nHost:      ^[hostname] (^[host IP])\nCheck:     ^[probe description]\nMessage:   ^[probe output]\nRun from:  ^[satellite description]\n\nTo acknowledge, reply to this message with this subject line:\n     ACK ^[alert id]\n\nTo immediately escalate, reply to this message with this subject line:\n     NACK ^[alert id]' where description = 'New Default (2.15)' ;

update rhn_notification_formats set body_format = 'This is Monitoring notification ^[alert id].\n\nTime:      ^[timestamp:"%a %b %d, %H:%M:%S %Z"]\nState:     ^[probe state]\nHost:      ^[hostname] (^[host IP])\nCheck:     ^[probe description]\nMessage:   ^[probe output]\nRun from:  ^[satellite description]' where description = 'New Default (2.18)' ;

