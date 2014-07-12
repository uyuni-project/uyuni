/**
 * Copyright (c) 2013--2014 Red Hat, Inc.
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

package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.dto.CrashSystemsDto;
import com.redhat.rhn.frontend.dto.IdenticalCrashesDto;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CrashFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.Crash / CrashFile objects from the database.
 * @version $Rev$
 */
public class CrashFactory extends HibernateFactory {

    private static CrashFactory singleton = new CrashFactory();
    private static Logger log = Logger.getLogger(CrashFactory.class);

    private CrashFactory() {
        super();
    }

    protected Logger getLogger() {
        return log;
    }

    /**
     * Lookup a Crash by its id
     * @param id the id to search for
     * @return the Crash found
     */
    public static Crash lookupById(Long id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        return (Crash) singleton.lookupObjectByNamedQuery(
                "Crash.findById", params);
    }

    /**
     * Delete a crash.
     * @param crash Crash to delete.
     */
    public static void delete(Crash crash) {
        singleton.removeObject(crash);
    }

    /**
     * Lookup a CrashFile by its id
     * @param id the id to search for
     * @return the CrashFile found
     */
    public static CrashFile lookupCrashFileById(Long id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        return (CrashFile) singleton.lookupObjectByNamedQuery(
                "CrashFile.findById", params);
    }

    /**
     * Lookup CrashNote by id and crash
     * @param crashNoteIdIn crash note id
     * @param crashIn crash
     * @return crash note for given id
     */
    public static CrashNote lookupCrashNoteByIdAndCrash(Long crashNoteIdIn,
            Crash crashIn) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", crashNoteIdIn);
        params.put("crash", crashIn);
        return (CrashNote) singleton.lookupObjectByNamedQuery(
                "CrashNote.findByIdAndCrash", params);
    }

    /**
     * @param crashNoteId Crash note id
     * @return Crash note for given id
     */
    public static CrashNote lookupCrashNoteById(Long crashNoteId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", crashNoteId);
        return (CrashNote) singleton.lookupObjectByNamedQuery(
                "CrashNote.findById", params);
    }

    /**
     * Lists crash notes of a specified crash
     * @param crashIn crash
     * @return crash notes for given crash
     */
    public static List<CrashNote> listCrashNotesByCrash(Crash crashIn) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("crash", crashIn);
        return singleton.listObjectsByNamedQuery(
                "CrashNote.listByCrash", params);
    }

    /**
     * Saves CrashNote object
     * @param crashNoteIn crash note to save
     */
    public static void save(CrashNote crashNoteIn) {
        singleton.saveObject(crashNoteIn);
    }

    /**
     * Deletes a CrashNote object
     * @param crashNoteIn crash note to delete
     */
    public static void delete(CrashNote crashNoteIn) {
        singleton.removeObject(crashNoteIn);
    }

    /**
     * List Software crashes for a given user and org grouped by crash uuid
     * @param userIn The org to return the list for
     * @param orgIn The org to return the list for
     * @return software crash list for a user and org group by crash uuid
     */
    public static List<IdenticalCrashesDto> listIdenticalCrashesForOrg(User userIn,
                                                                       Org orgIn) {
        SelectMode m = ModeFactory.getMode("Crash_queries",
                                           "list_identical_crashes_for_org");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", userIn.getId());
        params.put("org_id", orgIn.getId());
        return m.execute(params);
    }

    /**
     * List summary for software crashes with a given uuid
     * @param userIn The org to return the summary for
     * @param orgIn The org to return the summary for
     * @param uuidIn Crash uuid to return the summary for
     * @return software crash list for a user and org group by crash uuid
     */
    public static List<IdenticalCrashesDto> listCrashUuidDetails(User userIn,
                                                           Org orgIn,
                                                           String uuidIn) {
        SelectMode m = ModeFactory.getMode("Crash_queries",
                                           "list_crash_details_for_uuid");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", userIn.getId());
        params.put("org_id", orgIn.getId());
        params.put("uuid", uuidIn);
        return m.execute(params);
    }

    /**
     * List systems and further details showing crash with given uuid
     * @param userIn The org to return the list for
     * @param orgIn The org to return the list for
     * @param uuidIn Crash uuid to return the list for
     * @return List of systems and further details showing crash with given uuid
     */
    public static List<CrashSystemsDto> listCrashSystems(User userIn,
                                                         Org orgIn,
                                                         String uuidIn) {
        SelectMode m = ModeFactory.getMode("Crash_queries",
                                           "list_systems_for crash_uuid");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", userIn.getId());
        params.put("org_id", orgIn.getId());
        params.put("uuid", uuidIn);
        return m.execute(params);
    }
}
