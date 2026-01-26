/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.MethodUtil;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegate;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * TestUtils, a simple package for utility functions helpful when
 * writing unit tests
 */
public class TestUtils {

    /** Prefix for temporary file names created by this class. */
    private static String filePrefix = TestUtils.randomString();

    // static class
    private TestUtils() { }

    /**
     * method to find a file relative to the calling class.  primarily
     * useful when writing tests that need access to external data.
     * this lets you put the data relative to the test class file.
     *
     * @param path the path, relative to caller's location
     * @return URL a URL referencing the file
     * @throws ClassNotFoundException if the calling class can not be found
     * (i.e., should not happen)
     * @throws IOException if the specified file in an archive (eg. jar) and
     * it cannot be copied to a temporary location
     */
    public static URL findTestData(String path) throws ClassNotFoundException, IOException {
        Throwable t = new Throwable();
        StackTraceElement[] ste = t.getStackTrace();

        String className = ste[1].getClassName();
        Class clazz = Class.forName(className);

        URL ret = clazz.getResource(path);

        if (ret.toString().contains("!")) { // file is from an archive
            String tempPath = "/tmp/" + filePrefix + ret.hashCode();
            InputStream input = clazz.getResourceAsStream(path);

            OutputStream output = new FileOutputStream(tempPath);
            IOUtils.copy(input, output);

            return new File(tempPath).toURI().toURL();
        }

        return ret;
    }

    /**
     * method to find a file relative to the calling class.  primarily
     * useful when writing tests that need access to external data.
     * this lets you put the data relative to the test class file.
     * The data is extracted to a temp directory.
     *
     * @param path the path, relative to caller's location
     * @return URL a URL referencing the file
     * @throws ClassNotFoundException if the calling class can not be found
     * (i.e., should not happen)
     * @throws IOException if the specified file in an archive (eg. jar) and
     * it cannot be copied to a temporary location
     */
    public static URL findTestDataInDir(String path)
            throws ClassNotFoundException, IOException {
        Throwable t = new Throwable();
        StackTraceElement[] ste = t.getStackTrace();

        String className = ste[1].getClassName();
        Class clazz = Class.forName(className);

        URL ret = clazz.getResource(path);

        if (ret.toString().contains("!")) { // file is from an archive
            Path tmpDir = Files.createTempDirectory("testutils");
            String tempPath = tmpDir + "/" + StringUtils.substringAfterLast(path, "/");
            InputStream input = clazz.getResourceAsStream(path);

            OutputStream output = new FileOutputStream(tempPath);
            IOUtils.copy(input, output);

            return new File(tempPath).toURI().toURL();
        }

        return ret;
    }

    /**
     * Write a byte array to a file
     * @param file to write bytearray to
     * @param array to write out
     */
    public static void writeByteArrayToFile(File file, byte[] array) {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(array);
            }
            finally {
                fos.close();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Read whole stream into a string.
     *
     * @param stream the stream to consume
     * @return the contents of the stream as a string
     * @throws IOException if reading the file fails
     */
    public static String readAll(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder expected = new StringBuilder();
        while (reader.ready()) {
            expected.append(reader.readLine()).append("\n");
        }
        return expected.toString();
    }

    /**
     * Connect to <code>url</code> and return the contents
     * of that location as a string.
     *
     * @param url the URL to read from
     * @return the contents of the URL as a string, or <code>null</code>
     * if <code>url</code> is <code>null</code>
     * @throws IOException if reading the file fails
     */
    public static String readAll(URL url) throws IOException {
        if (url == null) {
            return null;
        }
        return readAll(url.openStream());
    }

    /**
     * Get a request with a Session and a User.
     * @return a request with a Session and a User.
     */
    public static RhnMockHttpServletRequest getRequestWithSessionAndUser() {
        RhnMockHttpServletRequest req = new RhnMockHttpServletRequest();
        RhnMockHttpServletResponse resp = new RhnMockHttpServletResponse();

        // Create a test user
        User user = UserTestUtils.createUser(
                TestStatics.TEST_USER,
                "testOrg_getRequestWithSessionAndUser" + RandomStringUtils.randomAlphanumeric(5)
        );
        Long userid = user.getId();

        // Set up the user context using PxtSessionDelegate
        PxtSessionDelegateFactory pxtDelegateFactory = PxtSessionDelegateFactory.getInstance();
        PxtSessionDelegate pxtDelegate = pxtDelegateFactory.newPxtSessionDelegate();

        // Update the web user id in the request context
        // required for getCurrentUser() to work
        pxtDelegate.updateWebUserId(req, resp, userid);

        // Set the uid parameter
        req.addCookie(resp.getCookie("pxt-session-cookie"));
        req.addParameter("uid", userid.toString());

        return req;
    }

    /**
     * Print the first few lines from a stacktrace so we can figure out who the caller
     * is.  This is similiar to doing a Thread.dumpStack() but it just doesn't spit out
     * as many lines of the stacktrace.
     *
     * @param depth the number of lines of the stacktrace you want to see
     */
    public static void printWhoCalledMe(int depth) {

        StackTraceElement[] elements = null;
        try {
            throw new Exception("Nothing to see here");
        }
        catch (Exception e) {
            elements = e.getStackTrace();
        }
        // Only show 10 lines of the trace
        for (int i = 0; ((i < elements.length) && i < depth); i++) {
            System.out.println("Stack: [" + i + "]: " + elements[i].getClassName() +
                               "." + elements[i].getMethodName() + " : " +
                               elements[i].getLineNumber());
        }
    }

    /**
     * Sets the Config to indicate we want to be in
     * Debug mode for Localization.  Usefull for checking
     * if a set of strings is l10ned.
     *
     */
    public static void enableLocalizationDebugMode() {
        Config.get().setString("java.l10n_debug", "true");
    }

    /**
    * Turns of the Config setting for L10N debug mode
    */
    public static void disableLocalizationDebugMode() {
        Config.get().setString("java.l10n_debug", "false");
    }

    /**
     * Disable the *** ERROR: Message with id: [asciiString] not found.***
     * errors.   Some tests pass in non-translated strings which is OK.
     */
    public static void disableLocalizationLogging() {
        Configurator.setLevel(LocalizationService.class.getName(), Level.OFF);
    }

    /**
     * Enable the *** ERROR: Message with id: [asciiString] not found.***
     * errors.   Some tests pass in non-translated strings which is OK.
     */
    public static void enableLocalizationLogging() {
        Configurator.setLevel(LocalizationService.class.getName(), Level.WARN);
    }


    /**
     * Check the string to see if it passed through the LocalizationService.
     * @param checkMe String to check if it was l10ned
     * @return boolean if or not it was localized
     */
    public static boolean isLocalized(String checkMe) {
        if (!Boolean.valueOf(
                Config.get().getString("java.l10n_debug", "false"))) {
            throw new
                IllegalArgumentException("java.l10n_debug is set to false.  " +
                        "This test doesnt mean anything if its set to false. ");
        }
        return (checkMe.startsWith(
            Config.get().getString("java.l10n_debug_marker", "$$$")));
    }

    /**
     * Check the request in the ActionHelper to validate that there is a UI
     * message in the session.  Useful for struts actions where you want to
     * verify that it put something in the session.
     * @param ah actionhelper used in the test
     * @param key to the i18n resource
     * @return boolean if it was found or not
     */
    public static boolean validateUIMessage(ActionHelper ah, String key) {
        ActionMessages mess = (ActionMessages)
            ah.getRequest().getSession().getAttribute(Globals.MESSAGE_KEY);
        if (mess == null) {
            return false;
        }
        ActionMessage am =  (ActionMessage) mess.get().next();
        String value = am.getKey();
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        return value.equals(key);
    }

    /**
     * Return a random 13 letter string.  Useful for creating unique
     * string labels/names in your tests.
     * @return String that is 13 chars long and alphanumeric
     */
    public static String randomString() {
        return randomString(13);
    }

    /**
     * Return a random letter string.  Useful for creating unique
     * string labels/names in your tests.
     * @param length of the string
     * @return A random alphanumeric string of the specified length
     */
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * Return a random numeric string
     * @param length of the string
     * @return A random numeric string of the specified length
     */
    public static String randomNumeric(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    /**
     * Run a test query
     * @param <T> type
     * @param mode to run
     * @param params map
     * @return DataResult List
     */
    public static <T> DataResult<T> runTestQuery(String mode, Map<String, ?> params) {
        SelectMode m =
            ModeFactory.
            getMode("test_queries", mode);
        return m.execute(params);
    }

    /**
     * Search an array by calling the passsed in method name with the key as the checker
     * @param search array
     * @param methodName to call on each object in the array (can be toString())
     * @param key to compare to
     * @return boolean if found or not
     */
    public static boolean arraySearch(Object[] search, String methodName, Object key) {
        boolean found = false;
        for (Object searchIn : search) {
            Object value = MethodUtil.callMethod(searchIn, methodName, new Object[0]);
            if (value.equals(key)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Get a private field from a class. Good for testing
     * the inner state of a class's member variables.
     *
     * Copied from: http://snippets.dzone.com/posts/show/2242
     *
     * @param o to check
     * @param fieldName to find
     * @return Object if found
     */
    public static Object getPrivateField(Object o, String fieldName) {
        /* Check we have valid arguments */
        /* Go and find the private field... */
        final Field[] fields = o.getClass().getDeclaredFields();
        for (Field fieldIn : fields) {
            if (fieldName.equals(fieldIn.getName())) {
                try {
                    fieldIn.setAccessible(true);
                    return fieldIn.get(o);
                }
                catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return null;
    }

    /**
     * Used to test the equals contract on objects.
     * The contract as specified by java.lang.Object states that if A.equals(B) is true
     * then B.equals(A) is also true. It also specifies that if A.equals(B) is true
     * then A.hashCode() will equals B.hashCode()
     * @param o1 object1
     * @param o2 object2
     * @return both objects equal
     */
    public static boolean equalTest(Object o1, Object o2) {
        // both null
        if (o1 == null && o2 == null) {
            return true;
        }
        // just one null
        if (o1 == null || o2 == null) {
            return false;
        }

        if (o1.equals(o2) != o2.equals(o1)) {
            return false;
        }
        return o1.hashCode() == o2.hashCode();
    }

    /**
     * Read a file relative to the given object package.
     * @param object the object
     * @param file path of the file to read
     * @return the content of the file as string.
     * @throws IOException in case of IO error
     * @throws ClassNotFoundException in case of classpath problems
     */
    public static String readRelativeFile(Object object,
                                          String file)
            throws IOException, ClassNotFoundException {
        return FileUtils.readFileToString(new File(TestUtils.findTestDataInDir(
                "/" + object.getClass().getPackage().getName()
                        .replaceAll("\\.", "/") + "/" + file).getPath()
        ));
    }

    //=========================================================================
    // HIBERNATE METHODS
    //=========================================================================

    /**
     * Finds a single instance of a persistent object.
     * @param query The query to find the persistent object should
     * be formulated to ensure a single object is returned or
     * an error will occur.
     * @return Object found or null if not
     */
    public static Object lookupTestObject(String query) {
        Session session = HibernateFactory.getSession();
        Query q = session.createQuery(query);
        return q.uniqueResult();
    }

    /**
     * Finds a list of persistent objects.
     * @param query The query to find the persistent objects.
     * @return Object found or null if not
     */
    public static List lookupTestObjects(String query) {
        Session session = HibernateFactory.getSession();
        Query q = session.createQuery(query);
        return q.list();
    }


    /**
     * Helper method to get a single object from the 2nd level cache by id
     * @param id Id of the object you want
     * @param queryname Queryname for the query you want to run.
     *        queryname *MUST* have an :id attribute in it.
     * @return Returns the object corresponding to id
     */
    public static Object lookupFromCacheById(Long id, String queryname) {
        Session session = HibernateFactory.getSession();
        return session.createNamedQuery(queryname, Object.class)
                .setParameter("id", id, StandardBasicTypes.LONG)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Helper method to get a single object from the 2nd level cache by id
     *
     * @param <T>      type of object to retrieve
     * @param id       id of the object to retrieve
     * @param objClass class name of the object to retrieve
     * @return Returns the object corresponding to the given id
     */
    public static <T> T lookupFromCacheById(Long id, Class<T> objClass) {
        Session session = HibernateFactory.getSession();
        return session.find(objClass, id);
    }

    /**
     * Helper method to get a single object from the 2nd level cache by label
     * @param label Label of the object you want
     * @param queryname Queryname for the query you want to run.
     *        queryname *MUST* have a :label attribute in it.
     * @return Returns the object corresponding to label
     */
    public static Object lookupFromCacheByLabel(String label,
                                                String queryname) {
        Session session = HibernateFactory.getSession();
        return session.createNamedQuery(queryname, Object.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Helper method to get a ChannelArch from the 2nd level cache by id
     * @param id Id of the ChannelArch
     * @return Returns the ChannelArch corresponding to id
     */
    public static ChannelArch lookupChannelArchFromCacheById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ChannelArch AS c WHERE c.id = :id", ChannelArch.class)
                .setParameter("id", id, StandardBasicTypes.LONG)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Helper method to get a ChannelArch from the 2nd level cache by label
     * @param label label of the ChannelArch
     * @return Returns the ChannelArch corresponding to label
     */
    public static ChannelArch lookupChannelArchFromCacheByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ChannelArch AS c WHERE c.label = :label", ChannelArch.class)
                .setParameter("label", label, StandardBasicTypes.STRING)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Util to flush and evict an object from the Hibernate Session
     * @param obj to flush
     * @throws HibernateException if something bad happens
     */
    public static void flushAndEvict(Object obj) throws HibernateException {
        Session session = HibernateFactory.getSession();
        session.flush();
        session.evict(obj);
    }

    /**
     * Util to reload an object using Hibernate
     * @param obj to be reloaded
     * @param <T> type of object to reload
     * @return Object found if not, null
     * @throws HibernateException if something bad happens.
     */
    public static <T> T reload(T obj) throws HibernateException {
        assertNotNull(obj);
        Session session = HibernateFactory.getSession();
        session.flush();

        if (session.contains(obj)) {
            session.detach(obj);
        }

        Serializable id = (Serializable) session.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(obj);
        return (T) session.find(obj.getClass(), id);
    }

    /**
     * Save and reload an object from DB
     * @param o to save and reload.
     * @param <T> type of object to save and reload
     * @return Object fresh from DB
     */
    public static <T> T saveAndReload(T o) {
        T managed = TestUtils.saveAndFlush(o);
        return reload(managed);
    }

    /**
     * Merge an object from DB
     * @param o to merge
     * @param <T> type of object to merge
     * @return Object fresh from DB
     */
    public static <T> T merge(T o) {
        Session session = HibernateFactory.getSession();
        session.merge(o);
        session.flush();
        return reload(o);
    }

    /**
     * Helper method to save objects to the database and flush
     * the session.
     * @param entity object to save.
     * @param <T> the entity type
     * @return the managed entity
     * @throws HibernateException HibernateException
     */
    public static <T> T save(T entity) throws HibernateException {
        Session session = HibernateFactory.getSession();

        // if the entity happens to be already managed, return it
        if (session.contains(entity)) {
            return entity;
        }

        Object id = session.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        T managed = entity;

        if (id == null) {
            // new entity - use persist() to avoid cascading issues
            session.persist(entity);
        }
        else {
            // detached entity - use merge() and return managed instance
            managed = session.merge(entity);
        }

        return managed;
    }


    public static <T> T saveAndFlush(T entity) throws HibernateException {
        T managed = save(entity);
        Session session = HibernateFactory.getSession();
        session.flush();
        return managed;
    }

    /**
     * Removes an object from the database.
     * @param toRemove Object to be removed.
     * @return Number of rows affected.
     */
    public static int removeObject(Object toRemove) {
        Session session = null;
        int numDeleted = 0;

        try {
            session = HibernateFactory.getSession();

            session.remove(toRemove);
            numDeleted++;

        }
        catch (HibernateException he) {
            throw new HibernateRuntimeException("Error removing " + toRemove, he);
        }

        return numDeleted;
    }


    /**
     * Clears hibernate session
     */
    public static void clearSession() {
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
    }

    /**
     * PLEASE Refrain from using this unless you really have to.
     *
     * Try clearSession() instead
     * @throws HibernateException hibernate exception
     */
    public static void commitAndCloseSession() throws HibernateException {
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }


}



