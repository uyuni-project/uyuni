
alter table web_contact_log drop column old_password;

alter table web_contact_log alter password type varchar(110);

select .recreate_trigger('web_contact');
