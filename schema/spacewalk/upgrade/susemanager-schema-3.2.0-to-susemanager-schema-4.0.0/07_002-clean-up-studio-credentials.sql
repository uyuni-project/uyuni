-- Delete all credentials of type SUSE Studio
delete from suseCredentials where type_id =
    (select id from suseCredentialsType where label = 'susestudio');

-- Delete the credentials type
delete from suseCredentialsType where label = 'susestudio';

