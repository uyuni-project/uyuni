/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.contentmgmt;

import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentManager;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * Content Management XMLRPC handler
 */
public class ContentManagementHandler extends BaseHandler {

    /**
     * List Content Projects visible to user
     *
     * @param loggedInUser - the logged in user
     * @return the list of Content Projects visible to user
     *
     * @xmlrpc.doc Look up Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #array()
     * $ContentProjectSerializer
     * #array_end()
     */
    public List<ContentProject> listProjects(User loggedInUser) {
        return ContentProjectFactory.listContentProjects(loggedInUser.getOrg());
    }

    /**
     * Look up Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the label
     * @return the Content Project with given label
     *
     * @xmlrpc.doc Look up Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject lookupProject(User loggedInUser, String label) {
        return ContentManager.lookupContentProject(label, loggedInUser).orElse(null);
    }

    /**
     * Create Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the label
     * @param name - the name
     * @param description - the description
     * @return the created Content Project
     *
     * @xmlrpc.doc Create Content Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject createProject(User loggedInUser, String label, String name, String description) {
        try {
            return ContentManager.createContentProject(label, name, description, loggedInUser);
        }
        catch (ContentManagementException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    /**
     * Update Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the new label
     * @param props - the map with the Content Project properties
     * @return the updated Content Project
     *
     * @xmlrpc.doc Update Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param
     *  #struct("data")
     *      #prop_desc("string", "name", "Content Project name")
     *      #prop_desc("string", "description", ""Content Project description")
     *  #struct_end()
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject updateProject(User loggedInUser, String label, Map<String, Object> props) {
        try {
            return ContentManager.updateContentProject(label,
                    ofNullable((String) props.get("name")),
                    ofNullable((String) props.get("description")),
                    loggedInUser);
        }
        catch (ContentManagementException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    /**
     * Remove Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the label
     * @return the number of removed objects
     *
     * @xmlrpc.doc Remove Content Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype int - the number of removed objects
     */
    public int removeContentProject(User loggedInUser, String label) {
        return ContentManager.removeContentProject(label, loggedInUser);
    }
}
