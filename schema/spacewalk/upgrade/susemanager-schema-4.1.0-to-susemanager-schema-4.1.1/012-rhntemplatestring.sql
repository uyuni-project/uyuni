update rhntemplatestring set value = '-' || '-<product_name /> Team', description = 'Footer for <product_name /> e-mail'  where label = 'email_footer';
update rhntemplatestring set value = 'Account Information:
Your <product_name /> login:         <login />
Your <product_name /> email address: <email-address />', description = 'Account info lines for <product_name /> e-mail' where label = 'email_account_info';


commit;


