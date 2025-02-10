SET search_path TO access, CURRENT;

-- View available operations for CM feature
SELECT namespace,
CASE
    WHEN access_mode = 'R' THEN 'View'
    ELSE 'Modify'
END AS access_mode, description
FROM namespace WHERE namespace LIKE 'cm.%';

-- Grant 'View' access to the user with ID=2
CALL grant_access(2, 'cm.%', 'R');

-- View user's access rules
SELECT * FROM userAccessTable WHERE user_id = 2;

-- See what the user can do with image stores
SELECT description FROM userNamespace un
JOIN namespace n ON un.namespace_id = n.id
WHERE user_id = 2
AND namespace LIKE 'cm.store.%';

-- Store details are sensitive, revoke access
CALL revoke_access(2, 'cm.store.details');

-- See what the user can do with images
SELECT description FROM userNamespace un
JOIN namespace n ON un.namespace_id = n.id
WHERE user_id = 2
AND namespace LIKE 'cm.%';

-- We want the user to be able to build images
-- Find the proper namespace
SELECT namespace, access_mode, description
FROM namespace
WHERE description ILIKE '%build%';

-- Grant build access
CALL grant_access(2, 'cm.image.overview');
CALL grant_access(2, 'cm.build');

-- See what the user can do with images
SELECT description FROM userNamespace un
JOIN namespace n ON un.namespace_id = n.id
WHERE user_id = 2
AND namespace LIKE 'cm.%';

-- See what endpoints are involved in CM
-- Useful when debugging unexpected 403 results from an endpoint
SELECT * from endpointCatalog WHERE namespace LIKE 'cm.%';

-- Done with user, revoke all access to CM
CALL revoke_access(2, 'cm.%');

-- Verify user's access rules
SELECT * FROM userAccessTable WHERE user_id = 2;
