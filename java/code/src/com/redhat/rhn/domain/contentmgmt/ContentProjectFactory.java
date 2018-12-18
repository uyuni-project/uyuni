/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.image.ImageInfoFactory;

import com.redhat.rhn.domain.org.Org;
import org.apache.log4j.Logger;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *  HibernateFactory for the {@link com.redhat.rhn.domain.contentmgmt.ContentProject} class.
 */
public class ContentProjectFactory extends HibernateFactory {

    private static ContentProjectFactory instance;
    private static Logger log = Logger.getLogger(ImageInfoFactory.class);

    private ContentProjectFactory() {
        super();
    }

    /**
     * Get the instance.
     * @return the instance
     */
    public static synchronized ContentProjectFactory getInstance() {
        if (instance == null) {
            instance = new ContentProjectFactory();
        }
        return instance;
    }

    /**
     * Save the ContentProject
     *
     * @param contentProject - the ContentProject
     */
    public void save(ContentProject contentProject) {
        saveObject(contentProject);
    }

    /**
     * Save a Content Environment in DB
     * @param contentEnvironment the content environment
     */
    public void save(ContentEnvironment contentEnvironment) {
        saveObject(contentEnvironment);
    }

    /**
     * Looks up a ContentProject by label
     *
     * @param label - the label
     * @return ContentProject with given label
     */
    public ContentProject lookupContentProjectByLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> criteria = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = criteria.from(ContentProject.class);
        criteria.where(builder.equal(root.get("label"), label));
        return getSession().createQuery(criteria).getSingleResult();
    }

    /**
     * List all ContentProjects with given organization
     * @param org - the organization
     * @return the ContentProjects in given organization
     */
    public List<ContentProject> listContentProjects(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ContentProject> query = builder.createQuery(ContentProject.class);
        Root<ContentProject> root = query.from(ContentProject.class);
        query.where(builder.equal(root.get("org"), org));
        return getSession().createQuery(query).list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
