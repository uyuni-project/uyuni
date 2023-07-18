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
package com.redhat.rhn.manager.audit;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.dto.AuditDto;
import com.redhat.rhn.frontend.dto.AuditMachineDto;
import com.redhat.rhn.frontend.dto.AuditReviewDto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AuditManager
 */
public class AuditManager /* extends BaseManager */ {

    private static Logger log = LogManager.getLogger(AuditManager.class);

    private AuditManager() {
    }

    private static String logDirStr = Config.get().getString("web.audit.logdir",
            "/var/spacewalk/systemlogs");
    private static File logDir = new File(logDirStr);
    private static File reviewFile = new File(logDirStr + "/audit-review.log");

    /**
     * Mark a machine/start/end as reviewed.
     * @param machine Machine name
     * @param start Start time in ms from epoch
     * @param end End time in ms from epoch
     * @param username User marking the review
     * @throws IOException Thrown when the audit review log isn't writeable
     */
    public static void markReviewed(String machine, Long start, Long end, String username) throws IOException {
        try (FileWriter fwr = new FileWriter(reviewFile, true)) { // append!
            fwr.write(machine + "," + (start / 1000) + "," + (end / 1000) + "," +
                username + "," + (new Date().getTime() / 1000) + "\n");
        }
    }

    /**
     * Retrieve the audits for a machine, possibly filtering by types and time
     * @param types The types to look for (e.g. "DAEMON_START"); can be null
     * @param machine The machine name
     * @param start The start time; can be null
     * @param end The end time; can be null
     * @return The set of matching audit logs
     */
    public static DataResult getAuditLogs(String[] types, String machine,
            Long start, Long end) {
        DataResult dr = null;
        List l;
        Long fileStart, fileEnd;

        if (types == null) {
            types = new String[0];
        }

        if (start == null) {
            start = 0L;
        }

        if (end == null) {
            end = Long.MAX_VALUE;
        }

        try {
            DataResult<AuditReviewDto> aureviewsections = getMachineReviewSections(machine);
            if (aureviewsections != null) {
                for (AuditReviewDto aureview : getMachineReviewSections(machine)) {
                    fileStart = aureview.getStart().getTime();
                    fileEnd = aureview.getEnd().getTime();

                    if (fileEnd < start || fileStart > end) {
                        continue;
                    }

                    File auditLog = new File(
                        logDirStr + "/" + aureview.getName() + "/audit/audit-" +
                        (fileStart / 1000) + "-" +
                        (fileEnd / 1000) + ".parsed");

                    l = readAuditFile(auditLog, types, start, end);

                    if (dr == null) {
                        dr = new DataResult(l);
                    }
                    else {
                        dr.addAll(l);
                    }
                }
            }
        }
        catch (IOException ioex) {
            log.warn("AAAAHHHH IOException", ioex);
        }

        if (dr == null || dr.isEmpty()) {
            return null;
        }

        return dr;
    }

    /**
     * Return the various audit type-mappings we have defined
     * @return A mapping between the set name and the audit types
     */
    public static Map<String, String[]> getAuditTypeMap() {
        Map<String, String[]> types = new HashMap<>();

        types.put("default", new String[]{
            "USER",
            "USER_AUTH",
            "USER_MGMT",
            "USER_ERR",
            "USER_LOGIN",
            "USER_LOGOUT",
            "ADD_USER",
            "DEL_USER",
            "ADD_GROUP",
            "DEL_GROUP",
            "DAEMON_START",
            "DAEMON_END",
            "DAEMON_ABORT",
            "DAEMON_CONFIG",
            "DAEMON_ROTATE",
            "DAEMON_RESUME",
            "CONFIG_CHANGE",
            "MAC_POLICY_LOAD",
            "MAC_STATUS",
            "MAC_CONFIG_CHANGE",
            "ANOM_PROMISCUOUS",
            "ANOM_ABEND",
            "ANOM_LOGIN_FAILURES",
            "ANOM_LOGIN_TIME",
            "ANOM_LOGIN_SESSIONS",
            "ANOM_LOGIN_ACCT",
            "ANOM_LOGIN_LOCATION",
            "ANOM_MAX_DAC",
            "ANOM_MAX_MAC",
            "ANOM_AMTU_FAIL",
            "ANOM_RBAC_FAIL",
            "ANOM_RBAC_INTEGRITY_FAIL",
            "ANOM_CRYPTO_FAIL",
            "ANOM_ACCESS_FS",
            "ANOM_EXEC",
            "ANOM_MK_EXEC",
            "ANOM_ADD_ACCT",
            "ANOM_DEL_ACCT",
            "ANOM_MOD_ACCT",
            "USER_ROLE_CHANGE",
            "ROLE_ASSIGN",
            "ROLE_REMOVE",
            "LABEL_OVERRIDE",
            "LABEL_LEVEL_CHANGE",
            "USER_LABELED_EXPORT",
            "USER_UNLABELED_EXPORT",
            "DEV_ALLOC",
            "DEV_DEALLOC",
            "FS_RELABEL"
        });

        types.put("login", new String[]{
            "ANOM_LOGIN_ACCT",
            "ANOM_LOGIN_FAILURES",
            "ANOM_LOGIN_LOCATION",
            "ANOM_LOGIN_SESSIONS",
            "ANOM_LOGIN_TIME",
            "ANOM_ROOT_TRANS",
            "USER_AUTH",
            "USER_ERR",
            "USER_LOGIN",
            "USER_ROLE_CHANGE"
        });

        return types;
    }

    /**
     * Get the time for the first unreviewed log for the specified machine
     * @param machineName The machine to find review times for
     * @return An AuditReviewDto for the machine's first unreviewed section
     */
    public static AuditReviewDto getFirstUnreviewed(String machineName) {
        AuditReviewDto firstUnreviewed = null;
        DataResult<AuditReviewDto> dr = getMachineReviewSections(machineName);

        for (AuditReviewDto aurev : dr) {
            if (aurev.getReviewedBy() == null) { // an unreviewed log!
                if (firstUnreviewed == null ||
                        aurev.getStart().getTime() <
                        firstUnreviewed.getStart().getTime()) {
                    firstUnreviewed = aurev;
                }
            }
        }

        return firstUnreviewed;
    }

    /**
     * Get the last time-of-review for the specified machine
     * @param machineName The machine to find review times for
     * @return An AuditReviewDto for the machine's last review time
     */
    public static AuditReviewDto getLastReview(String machineName) {
        AuditReviewDto lastReviewed = null;
        DataResult<AuditReviewDto> dr = getMachineReviewSections(machineName);

        for (AuditReviewDto aurev : dr) {
            if (aurev.getReviewedOn() != null) {
                if (lastReviewed == null ||
                        aurev.getReviewedOn().getTime() >
                        lastReviewed.getReviewedOn().getTime()) {
                    lastReviewed = aurev;
                }
            }
        }

        return lastReviewed;
    }

    /**
     * Retrieve the set of all machines we know about
     * @return The set of machines
     */
    public static DataResult<AuditMachineDto> getMachines() {
        AuditReviewDto aurev;
        DataResult<AuditMachineDto> dr;
        Date lastReview, firstUnreviewed;
        LinkedList<AuditMachineDto> hosts = new LinkedList<>();

        if (logDir == null || !logDir.canRead()) {
            return new DataResult<>(hosts);
        }

        for (File host : logDir.listFiles()) {
            if (host.isDirectory()) {
                aurev = getLastReview(host.getName());

                if (aurev != null) {
                    lastReview = aurev.getReviewedOn();
                }
                else {
                    lastReview = null;
                }

                aurev = getFirstUnreviewed(host.getName());

                if (aurev != null) {
                    firstUnreviewed = aurev.getStart();
                }
                else {
                    firstUnreviewed = null;
                }

                hosts.add(new AuditMachineDto(
                    host.getName(), lastReview, firstUnreviewed));
            }
        }

        Collections.sort(hosts);
        dr = new DataResult<>(hosts);

        return dr;
    }

    /**
     * Retrieve the set of audit sections, possibly for a specified machine
     * @param machineName The machine to get review sections for; can be null
     * @return The set of review sections
     */
    public static DataResult<AuditReviewDto> getMachineReviewSections(
            String machineName) {
        long start, end;
        DataResult<AuditReviewDto> dr, rec;
        File hostDir;
        LinkedList<AuditReviewDto> aurevs = new LinkedList<>();
        Matcher fnmatch;
        Pattern fnregex = Pattern.compile("audit-(\\d+)-(\\d+).parsed");

        // if machineName is null, look up all review sections by recursion
        if (machineName == null || machineName.isEmpty()) {
            dr = null;

            for (AuditMachineDto aumachine : getMachines()) {
                if (aumachine.getName() != null) {
                    rec = getMachineReviewSections(aumachine.getName());

                    if (dr == null) {
                        dr = rec;
                    }
                    else {
                        dr.addAll(rec);
                    }
                }
            }

            return dr;
        }

        // otherwise, just look up this one machine
        hostDir = Path.of(logDirStr, machineName.replace(File.separator, ""), "audit").toFile();

        if (!hostDir.exists()) {
            return new DataResult(new LinkedList<>());
        }

        for (String auditLog : hostDir.list()) {
            fnmatch = fnregex.matcher(auditLog);

            if (fnmatch.matches()) { // found a matching audit file
                start = Long.parseLong(fnmatch.group(1)) * 1000;
                end = Long.parseLong(fnmatch.group(2)) * 1000;

                try { // but is it reviewed yet?
                    aurevs.add(getReviewInfo(machineName, start, end));
                }
                catch (IOException ioex) { // on error, assume unreviewed
                    aurevs.add(new AuditReviewDto(machineName, new Date(start),
                        new Date(end), null, null));
                }
            }
        }

        Collections.sort(aurevs);
        dr = new DataResult<>(aurevs);

        return dr;
    }

    /**
     * Retrieve the review info for a specified machine/time
     * @param machine The machine name
     * @param start The start time in ms from the epoch
     * @param end The end time in ms from the epoch
     * @throws IOException Throws when the audit review file is unreadable
     * @return An AuditReviewDto, possibly with review info set
     */
    public static AuditReviewDto getReviewInfo(String machine, long start, long end) throws IOException {
        Date reviewedOn = null;
        String str, part1, reviewedBy = null;
        String[] revInfo;

        part1 = machine + "," + (start / 1000) + "," + (end / 1000) + ",";

        try (BufferedReader brdr = new BufferedReader(new FileReader(reviewFile))) {

            while ((str = brdr.readLine()) != null) {
                if (str.startsWith(part1)) {
                    revInfo = str.split(",");
                    reviewedBy = revInfo[3];
                    reviewedOn = new Date(Long.parseLong(revInfo[4]) * 1000);
                    break;
                }
            }

            return new AuditReviewDto(machine, new Date(start), new Date(end), reviewedBy, reviewedOn);
        }
    }

    private static List<AuditDto> readAuditFile(File aufile, String[] types, Long start, Long end) throws IOException {
        List<AuditDto> events = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(aufile))) {
            Map<String, String> hmap = new LinkedHashMap<>();

            for (String str = reader.readLine(); str != null; str = reader.readLine()) {
                if (str.equals("")) {
                    int serial = getSerial(hmap);
                    long time = getTime(hmap);

                    if (time >= start && time <= end && Arrays.asList(types).contains(hmap.get("type"))) {
                        events.add(new AuditDto(serial, new Date(time), 0, null, hmap));
                    }

                    hmap.clear();
                }
                else if (str.indexOf('=') >= 0) {
                    hmap.put(
                        str.substring(0, str.indexOf('=')).trim(),
                        str.substring(str.indexOf('=') + 1).trim());
                }
                else {
                    log.debug("unknown string: {}", str);
                }
            }

            return events;
        }
    }

    private static Long getTime(Map<String, String> hmap) {
        try {
            return Long.parseLong(hmap.remove("seconds")) * 1000;
        }
        catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static int getSerial(Map<String, String> hmap) {
        try {
            return Integer.parseInt(hmap.remove("serial"));
        }
        catch (NumberFormatException nfex) {
            return -1;
        }
    }
}
