/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.common.db.datasource;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.manifestfactory.ManifestFactory;
import com.redhat.rhn.common.util.manifestfactory.ManifestFactoryBuilder;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Class to drive parsing of the DataSource XML files, and return modes
 * as they are requested.
 *
 */
public class ModeFactory implements ManifestFactoryBuilder {

    private static Logger logger = Logger.getLogger(ModeFactory.class);

    private static final String DEFAULT_PARSER_NAME =
                                       "org.apache.xerces.parsers.SAXParser";

    private static final String POSTGRES_QUERY_SUFFIX = "_pg";

    private static ManifestFactory factory = new ManifestFactory(new ModeFactory());

    private static XMLReader parser = null;

    /** {@inheritDoc} */
    public String getManifestFilename() {
        return "xml/file-list.xml";
    }

    /** {@inheritDoc} */
    public Object createObject(Map params) {
        try {
            if (parser == null) {
                parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);

                ContentHandler handler = new DataSourceParserHelper();
                parser.setContentHandler(handler);
            }
            String filename = (String)params.get("filename");
            if (filename == null) {
                throw new NullPointerException("filename is null");
            }

            logger.debug("Parsing mode file '" + filename + "'");
            URL u = this.getClass().getResource(filename);
            DataSourceParserHelper handler =
                          (DataSourceParserHelper)parser.getContentHandler();
            parser.parse(new InputSource(u.openStream()));
            return handler.getModes();
        }
        catch (Exception e) {
            throw new DataSourceParsingException("Unable to parse file", e);
        }
    }

    /**
     * Adapts the modeName parameter to specific DB system used.
     * Returns the modeName with no changes as default behavior.
     * @param modeName the mode name
     * @return The new modename
     */
    private static String adaptModeNameToDBSystem(String modeName) {
        if (ConfigDefaults.get().isPostgresql()) {
            return modeName + POSTGRES_QUERY_SUFFIX;
        }
        return modeName;
    }

    private static Mode getModeInternal(String name, String mode) {
        Session session = HibernateFactory.getSession();
        return getModeInternal(session, name, mode);

    }

    private static Mode getModeInternal(Session session, String name, String mode) {
        Map modes = (Map)factory.getObject(name);
        ParsedMode pm = (ParsedMode)modes.get(mode);
        if (pm == null) {
            throw new ModeNotFoundException(
                              "Could not find mode " + mode + " in " + name);
        }
        switch (pm.getType()) {
        case SELECT:
            return new SelectMode(session, pm);
        case CALLABLE:
            return new CallableMode(session, pm);
        case WRITE:
            return new WriteMode(session, pm);
        default:
            // should never reach here
            return null;
        }
    }

    private static SelectMode getSelectMode(String name, String mode) {
        Session session = HibernateFactory.getSession();
        return getSelectMode(session, name, mode);
    }

    private static SelectMode getSelectMode(Session session, String name, String mode) {
        Map modes = (Map) factory.getObject(name);
        ParsedMode pm = (ParsedMode) modes.get(mode);
        if (pm == null) {
            throw new ModeNotFoundException(
                              "Could not find mode " + mode + " in " + name);
        }
        return new SelectMode(session, pm);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static SelectMode getMode(String name, String mode) {
        return getSelectMode(name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param session hibernate session
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static SelectMode getMode(Session session, String name, String mode) {
        return getSelectMode(session, name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @param rdbmsSpecific Whether to retrieve the WriteMode for a specific rdbms.
     * @return The requested mode
     */
    public static SelectMode getSelectMode(String name, String mode, boolean rdbmsSpecific) {
        if (rdbmsSpecific) {
            return getSelectMode(name, adaptModeNameToDBSystem(mode));
        }
        return getSelectMode(name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param session hibernate session
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @param rdbmsSpecific Whether to retrieve the WriteMode for a specific rdbms.
     * @return The requested mode
     */
    public static SelectMode getSelectMode(Session session, String name, String mode, boolean rdbmsSpecific) {
        if (rdbmsSpecific) {
            return getSelectMode(session, name, adaptModeNameToDBSystem(mode));
        }
        return getSelectMode(session, name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed.
     * @param name The name of the file to search, this is the name as it is passed
     *             to parseURL.
     * @param mode The mode to retrieve
     * @param clazz The class you would like the returned objects to be.
     * @return The requested mode
     */
    public static SelectMode getMode(String name, String mode, Class clazz) {
        SelectMode ret = getSelectMode(name, mode);
        ret.setClassString(clazz.getName());
        return ret;
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed.
     * @param session hibernate session
     * @param name The name of the file to search, this is the name as it is passed
     *             to parseURL.
     * @param mode The mode to retrieve
     * @param clazz The class you would like the returned objects to be.
     * @return The requested mode
     */
    public static SelectMode getMode(Session session, String name, String mode, Class clazz) {
        SelectMode ret = getSelectMode(session, name, mode);
        ret.setClassString(clazz.getName());
        return ret;
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static WriteMode getWriteMode(String name, String mode) {
        return (WriteMode)getModeInternal(name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param session hibernate session
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static WriteMode getWriteMode(Session session, String name, String mode) {
        return (WriteMode)getModeInternal(session, name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @param rdbmsSpecific Whether to retrieve the WriteMode for a specific rdbms.
     * @return The requested mode
     */
    public static WriteMode getWriteMode(String name, String mode, boolean rdbmsSpecific) {
        if (rdbmsSpecific) {
            return (WriteMode)getModeInternal(name, adaptModeNameToDBSystem(mode));
        }
        return (WriteMode)getModeInternal(name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param session hibernate database session to be used
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @param rdbmsSpecific Whether to retrieve the WriteMode for a specific rdbms.
     * @return The requested mode
     */
    public static WriteMode getWriteMode(Session session, String name, String mode, boolean rdbmsSpecific) {
        if (rdbmsSpecific) {
            return (WriteMode)getModeInternal(session, name, adaptModeNameToDBSystem(mode));
        }
        return (WriteMode)getModeInternal(session, name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static CallableMode getCallableMode(String name, String mode) {
        return (CallableMode)getModeInternal(name, mode);
    }

    /**
     * Retrieve a specific mode from the map of modes already parsed
     * @param session hibernate session
     * @param name The name of the file to search, this is the name as it is
     *             passed to parseURL.
     * @param mode the mode to retrieve
     * @return The requested mode
     */
    public static CallableMode getCallableMode(Session session, String name, String mode) {
        return (CallableMode)getModeInternal(session, name, mode);
    }

    /**
     * Retrieve the keys
     * @return the fileMap filled out from parsing the files.
     * This function really shouldn't be here, but I need it for the
     * unit tests.
     */
    public static Collection getKeys() {
        return factory.getKeys();
    }

    /**
     * Retrieve the Modes for a given key.
     * @param name of the filemap to retrieve.
     * @return the fileMap filled out from parsing the files.
     * This function really shouldn't be here, but I need it for the
     * unit tests.
     */
    public static Map getFileKeys(String name) {
        return (Map)factory.getObject(name);
    }
}

