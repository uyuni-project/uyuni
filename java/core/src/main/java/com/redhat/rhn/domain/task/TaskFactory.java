/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.task;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.taskomatic.task.errata.ErrataCacheWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * TaskFactory
 */
public class TaskFactory extends HibernateFactory {

    private static TaskFactory singleton = new TaskFactory();
    private static Logger log = LogManager.getLogger(TaskFactory.class);

    private TaskFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Creates a new Task object.
     * @param org The org to which this task will belong
     * @param name A name for the task
     * @param data The data for this task (usually corresponds to an object id)
     * @return Returns the newly created task object.
     */
    public static Task createTask(Org org, String name, Long data) {
        Task t = new Task();
        t.setPriority(0); //default
        t.setOrg(org);
        t.setName(name);
        t.setData(data);
        t.setEarliest(new Date()); //set to now
        return save(t);
    }

    /**
     * Saves the object to the db
     * @param taskIn The task to save
     * @return the managed {@link Task} instance
     */
    public static Task save(Task taskIn) {
        return singleton.saveObject(taskIn);
    }

    /**
     * Remove a completed Task from the queue.
     *
     * @param taskIn to remove
     */
    public static void remove(Task taskIn) {
        singleton.removeObject(taskIn);
    }


    /**
     * Lookups up a task.
     * @param org The org containing the task
     * @param name The name of the task
     * @param data The data in the task
     * @return Returns the task that matches all three parameters or null.
     */
    public static Task lookup(Org org, String name, Long data) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Task> criteriaQuery = builder.createQuery(Task.class);
        Root<Task> root = criteriaQuery.from(Task.class);
        criteriaQuery.select(root).where(builder.and(
                builder.equal(root.get("name"), name),
                builder.equal(root.get("data"), data),
                builder.equal(root.get("org"), org)
        ));

        return session.createQuery(criteriaQuery).getResultList().stream().findFirst().orElse(null);
    }

    /**
     * Delete tasks matching an organization, a name, a data and a priority.
     *
     * @param org The organization
     * @param name the tasks name
     * @param data the tasks data
     * @param priority the tasks priority
     */
    public static void deleteByOrgNameDataPriority(Org org, String name, Long data, int priority) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaDelete<Task> criteriaDelete = builder.createCriteriaDelete(Task.class);
        Root<Task> root = criteriaDelete.from(Task.class);
        criteriaDelete.where(builder.and(
                builder.equal(root.get("name"), name),
                builder.equal(root.get("data"), data),
                builder.equal(root.get("org"), org),
                builder.equal(root.get("priority"), priority)
        ));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    /**
     * Delete tasks matching a name and a data, ignoring priority and organization.
     *
     * @param name the tasks name
     * @param data the tasks data
     */
    public static void deleteByNameData(String name, Long data) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaDelete<Task> criteriaDelete = builder.createCriteriaDelete(Task.class);
        Root<Task> root = criteriaDelete.from(Task.class);
        criteriaDelete.where(builder.and(
                builder.equal(root.get("name"), name),
                builder.equal(root.get("data"), data)
        ));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    /**
     * Gets the list of "update errata cache for channel" tasks.
     * @param org The org containing the tasks
     * @return Returns a list of task objects
     */
    public static List<Task> getTaskListByChannel(Org org) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Task> criteriaQuery = builder.createQuery(Task.class);
        Root<Task> root = criteriaQuery.from(Task.class);
        criteriaQuery.select(root).where(builder.and(
                builder.equal(root.get("name"), ErrataCacheWorker.BY_CHANNEL),
                builder.equal(root.get("org"), org)
        ));

        return session.createQuery(criteriaQuery).list();
    }



    /**
     * Lookup a list of Tasks who's name start with passed in param
     * @param nameIn to lookup
     * @return List of Tasks or null if not found.
     */
    public static List<Task> getTaskListByNameLike(String nameIn) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Task> criteriaQuery = builder.createQuery(Task.class);
        Root<Task> root = criteriaQuery.from(Task.class);
        criteriaQuery.select(root).where(builder.like(root.get("name"), nameIn + "%"));

        return session.createQuery(criteriaQuery).list();
    }
}
