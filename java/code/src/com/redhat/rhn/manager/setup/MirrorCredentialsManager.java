/**
 * Copyright (c) 2014 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.MgrSyncUtils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract base class for working with mirror credentials.
 */
public abstract class MirrorCredentialsManager {

    /**
     * Create and return an instance of {@link MirrorCredentialsManager}.
     *
     * @return instance of {@link MirrorCredentialsManager}
     */
    public static MirrorCredentialsManager createInstance() {
        return MgrSyncUtils.isMigratedToSCC() ? new SCCMirrorCredentialsManager() :
                new NCCMirrorCredentialsManager();
    }

    /**
     * Find all currently available mirror credentials and return them.
     *
     * @return list of all available mirror credentials
     */
    public abstract List<MirrorCredentialsDto> findMirrorCredentials();

    /**
     * Find mirror credentials for a given ID.
     *
     * @param id of the credentials to find
     * @return credentials for given ID
     */
    public abstract MirrorCredentialsDto findMirrorCredentials(long id);

    /**
     * Store mirror credentials given as {@link MirrorCredentialsDto}.
     *
     * @param creds the mirror credentials to store
     * @param user the current user
     * @param request the current HTTP request object (used for session caching)
     * @return id of the stored mirror credentials
     * @throws ContentSyncException in case of errors
     */
    public abstract long storeMirrorCredentials(MirrorCredentialsDto creds, User user,
            HttpServletRequest request) throws ContentSyncException;

    /**
     * Delete credentials given by ID.
     *
     * @param id the id of credentials to be deleted
     * @param user the current user
     * @param request the current HTTP request object (used for session caching)
     * @return list of validation errors or null in case of success
     */
    public abstract ValidatorError[] deleteMirrorCredentials(Long id, User user,
            HttpServletRequest request);

    /**
     * Make primary credentials for a given credentials ID.
     *
     * @param id the id of credentials to make the primary creds
     * @param user the current user
     * @param request the current HTTP request
     * @return list of validation errors or null in case of success
     */
    public abstract ValidatorError[] makePrimaryCredentials(Long id, User user,
            HttpServletRequest request);

    /**
     * Return cached list of subscriptions or "null" for signaling "verification failed".
     *
     * @param creds the credentials
     * @param request the current HTTP request
     * @param forceRefresh set true to refresh the cached subscriptions
     * @return list of subscriptions or null for "verification failed"
     */
    public abstract List<SubscriptionDto> getSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request, boolean forceRefresh);
}
