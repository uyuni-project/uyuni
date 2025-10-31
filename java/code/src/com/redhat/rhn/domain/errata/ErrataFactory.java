/*
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
/*
 * Copyright (c) 2010 SUSE LLC
 */
package com.redhat.rhn.domain.errata;

import static com.redhat.rhn.domain.errata.AdvisoryStatus.RETRACTED;

import com.redhat.rhn.common.db.DatabaseException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.common.ChecksumFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.ErrataPackageFile;
import com.redhat.rhn.frontend.dto.OwnedErrata;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;

import com.suse.utils.Opt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

/**
 * ErrataFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.errata.Errata objects from the
 * database.
 */
public class ErrataFactory extends HibernateFactory {

    private static final String ERRATA_QUERIES = "Errata_queries";
    private static ErrataFactory singleton = new ErrataFactory();
    private static Logger log = LogManager.getLogger(ErrataFactory.class);

    public static final String ERRATA_TYPE_BUG = "Bug Fix Advisory";
    public static final String ERRATA_TYPE_ENHANCEMENT = "Product Enhancement Advisory";
    public static final String ERRATA_TYPE_SECURITY = "Security Advisory";

    private ErrataFactory() {
        super();
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * List the package ids that were pushed to a channel because of an errata
     * @param cid the channel id
     * @param eid the errata id
     * @return List of package ids
     */
    public static List<Long> listErrataChannelPackages(Long cid, Long eid) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel_id", cid);
        params.put("errata_id", eid);
        DataResult<ErrataPackageFile> dr = executeSelectMode(
                "ErrataCache_queries",
                "package_associated_to_errata_and_channel", params);
        return dr.stream().map(file -> file.getPackageId()).collect(Collectors.toList());
    }

    /**
     * Tries to locate errata based on either the errataum's id or the
     * CVE/CAN identifier string.
     * @param identifier erratum id or CVE/CAN id string
     * @param org User organization
     * @return list of erratas found
     */
    public static List<Errata> lookupByIdentifier(String identifier, Org org) {
        Long eid = null;
        List<Errata> retval = new LinkedList<>();
        try {
            eid = Long.parseLong(identifier);
        }
        catch (NumberFormatException e) {
            // Nothing to do
        }
        if (eid != null) {
            Errata errata = ErrataFactory.lookupErrataById(eid);
            if (errata != null) {
                retval.add(errata);
            }
        }
        else if (identifier.length() > 4) {
            String prefix = null;
            List<Errata> erratas = ErrataFactory.lookupByAdvisoryId(identifier, org);
            if (erratas != null && !erratas.isEmpty()) {
                retval.addAll(erratas);
            }
            else {
                erratas = ErrataFactory.lookupVendorAndUserErrataByAdvisoryAndOrg(identifier, org);
                if (erratas != null && !erratas.isEmpty()) {
                    retval.addAll(erratas);
                }
            }
            if (retval.isEmpty()) {
                prefix = identifier.substring(0, 4);
                if (prefix.matches("RH.A")) {
                    StringTokenizer strtok = new StringTokenizer(identifier, "-");
                    StringBuilder buf = new StringBuilder();
                    boolean foundFirst = false;
                    while (strtok.hasMoreTokens()) {
                        buf.append(strtok.nextToken());
                        if (!foundFirst) {
                            buf.append("-");
                            foundFirst = true;
                        }
                        else {
                            if (strtok.hasMoreTokens()) {
                                buf.append(":");
                            }
                        }
                    }
                    identifier = buf.toString();
                    erratas = ErrataFactory.lookupByAdvisoryId(identifier, org);

                    if (erratas != null && !erratas.isEmpty()) {
                        retval.addAll(erratas);
                    }
                }
            }
            if (retval.isEmpty()) {
                prefix = identifier.substring(0, 3);
                if ((prefix.equals("CVE") || prefix.equals("CAN")) &&
                        identifier.length() > 7 && identifier.indexOf('-') == -1) {
                    identifier = identifier.substring(0, 3) + "-" +
                            identifier.substring(3, 7) + "-" +
                            identifier.substring(7);
                }
                erratas = ErrataFactory.lookupByCVE(identifier);
                retval.addAll(erratas);
            }
        }
        return retval;
    }

    /**
     * Takes an errata and adds it to a channel, creating all of the correct ErrataFile* entries.
     * This method does push packages to the appropriate channel. (Appropriate as defined as the
     * channel previously having a package with the same name).
     * @param errataList list of errata to add
     * @param chan channel to add them to
     * @param user the user adding errata to channel
     * @param inheritPackages include only original channel packages
     * @return the added errata
     */
    public static List<Errata> addToChannel(List<Errata> errataList, Channel chan,
                                            User user, boolean inheritPackages) {
        return addToChannel(errataList, chan, user, inheritPackages, true);
    }

    /**
     * Takes an errata and adds it to a channel, creating all of the correct ErrataFile* entries.
     * entries. This method does push packages to the appropriate channel.
     * (Appropriate as defined as the channel previously having a package with the same name).
     * @param errataList list of errata to add
     * @param chan channel to add them to
     * @param user the user doing the pushing
     * @param inheritPackages include only original channel packages
     * @param performPostActions true (default) if you want to refresh newest package
     * cache and schedule repomd regeneration. False only if you're going to do those
     * things yourself.
     * @return the added errata
     */
    public static List<Errata> addToChannel(List<Errata> errataList, Channel chan,
                                            User user, boolean inheritPackages, boolean performPostActions) {
        List<com.redhat.rhn.domain.errata.Errata> toReturn = new ArrayList<>();
        for (Errata errata : errataList) {
            errata.addChannel(chan);
            ErrataManager.replaceChannelNotifications(errata.getId(), chan.getId(), new Date());

            Set<Package> packagesToPush = new HashSet<>();
            DataResult<PackageOverview> packs;
            if (inheritPackages) {

                if (!chan.isCloned()) {
                    throw new InvalidChannelException("Cloned channel expected: " +
                            chan.getLabel());
                }
                Channel original = chan.getOriginal();
                // see BZ 805714, if we are a clone of a clone the 1st clone
                // may not have the errata we want
                Set<Channel> associatedChannels = errata.getChannels();
                while (original.isCloned() &&
                        !associatedChannels.contains(original)) {
                    original = ChannelFactory.lookupOriginalChannel(original);
                }
                packs = ErrataManager.listErrataChannelPacks(original, errata, user);
            }
            else {
                packs = ErrataManager.lookupPacksFromErrataForChannel(chan, errata, user);
            }

            for (PackageOverview packOver : packs) {
                //lookup the Package object
                Package pack = PackageFactory.lookupByIdAndUser(
                        packOver.getId(), user);
                packagesToPush.add(pack);
            }

            Errata e = addErrataPackagesToChannel(errata, chan, user, packagesToPush);
            toReturn.add(e);
        }
        if (performPostActions) {
            ChannelManager.refreshWithNewestPackages(chan, "java::addErrataPackagesToChannel");
        }
        return toReturn;
    }

    /**
     * Add an errata to a channel but only push a small set of packages
     * along with it
     *
     * @param errata errata to add
     * @param chan channel to add it to
     * @param user the user doing the adding
     * @param packages the packages to add
     * @return the added errata
     */
    public static Errata addToChannel(Errata errata, Channel chan, User user,
                                      Set<Package> packages) {
        errata.addChannel(chan);
        errata = addErrataPackagesToChannel(errata, chan, user, packages);
        ChannelManager.refreshWithNewestPackages(chan, "java::addErrataPackagesToChannel");
        return errata;
    }

    /**
     * Private helper method that pushes errata packages to a channel
     */
    private static Errata addErrataPackagesToChannel(Errata errata,
                                                     Channel chan, User user, Set<Package> packages) {
        // Much quicker to push all packages at once
        List<Long> pids = new ArrayList<>();
        for (Package pack : packages) {
            pids.add(pack.getId());
        }
        ChannelManager.addPackages(chan, pids, user);

        for (Package pack : packages) {
            if (pack.getPath() == null) {
                throw new DatabaseException("Package " + pack.getId() +
                    " has NULL path, please run spacewalk-data-fsck");
            }

            Optional<ErrataFile> fileOpt =
                    ErrataFactory.lookupErrataFile(errata.getId(), pack.getPath());

            singleton.saveObject(Opt.fold(fileOpt, () -> createErrataFile(pack, errata, chan),
                    ef -> addErrataFile(ef, pack, chan)));

        }
        ChannelFactory.save(chan);

        ErrataCacheManager.insertCacheForChannelErrataAsync(List.of(chan.getId()), errata);

        return errata;
    }

    /**
     * Private helper method that adds an ErrataFile to a channel.
     *
     * @param file ErrataFile to add
     * @param pack the Package to add
     * @param chan Channel to add it to
     * @return the added errata file
     */
    private static ErrataFile addErrataFile(ErrataFile file, Package pack, Channel chan) {
        if (!file.hasPackage(pack)) {
            file.addPackage(pack);
        }
        file.addChannel(chan);
        return file;
    }

    /**
     * Private helper method that creates an ErrataFile and adds it to a channel
     *
     * @param pack the Package to add
     * @param errata the Errata to add
     * @param chan Channel to add the package to
     * @return the added errata file
     */
    private static ErrataFile createErrataFile(Package pack, Errata errata, Channel chan) {
        var file = ErrataFactory.createErrataFile(
                ErrataFactory.lookupErrataFileType("RPM"), pack.getChecksum().getChecksum(), pack.getPath());
        file.addPackage(pack);
        file.setOwningErrata(errata);
        file.setModified(new Date());
        file.addChannel(chan);
        return file;
    }

    /**
     * Creates a new Bug object with the given details.
     * @param id The id for the new bug
     * @param summary The summary for the new bug
     * @param url The bug URL
     * @return The new bug.
     */
    public static Bug createBug(Long id, String summary, String url) {
        Bug bug = new Bug();
        bug.setId(id);
        bug.setSummary(summary);
        bug.setUrl(url);
        return bug;
    }

    /**
     * Creates a new Errata file with given ErrataFileType, checksum, and name
     * @param ft ErrataFileType for the new ErrataFile
     * @param cs Checksum for the new Errata File
     * @param name name for the file
     * @return new Errata File
     */
    public static ErrataFile createErrataFile(ErrataFileType ft, String cs, String name) {
        return createErrataFile(ft, cs, name, new HashSet<>());
    }

    /**
     * Creates a new Errata file with given ErrataFileType, checksum, and name
     * @param ft ErrataFileType for the new ErrataFile
     * @param cs Checksum for the new Errata File
     * @param name name for the file
     * @param packages Packages associated with this errata file.
     * @return new Errata File
     */
    public static ErrataFile createErrataFile(ErrataFileType ft,
                                              String cs,
                                              String name,
                                              Set<Package> packages) {
        ErrataFile file = new ErrataFile();
        file.setFileType(ft);
        file.setChecksum(ChecksumFactory.safeCreate(cs, ChecksumFactory.guessChecksumTypeByLength(cs)));
        file.setFileName(name);
        file.setPackages(packages);
        return file;
    }

    /**
     * Lookup a ErrataFileType based on a label
     * @param label file type label (RPM, IMG, etc)
     * @return ErrataFileType instance
     */
    public static ErrataFileType lookupErrataFileType(String label) {
        ErrataFileType retval;
        try {
            retval = singleton.lookupObjectByParam(ErrataFileType.class, "label", label, true);
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException(e.getMessage(), e);
        }
        return retval;
    }

    /**
     * Lookup ErrataFiles by errata and file type
     * @param errataId errata id
     * @param fileType file type label
     * @return list of ErrataFile instances
     */
    public static List<ErrataFile> lookupErrataFilesByErrataAndFileType(Long errataId, String fileType) {
        List<ErrataFile> retval;
        try {
            retval = getSession().createQuery("""
                                    FROM ErrataFile AS pef
                                    WHERE pef.owningErrata.id = :errata_id
                                    AND pef.fileType.label = :file_type
                                    """,
                            ErrataFile.class)
                    .setParameter("errata_id", errataId, StandardBasicTypes.LONG)
                    .setParameter("file_type", fileType.toUpperCase(), StandardBasicTypes.STRING)
                    .list();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException(e.getMessage(), e);
        }
        return retval;
    }

    /**
     * Lookup a Errata by their id
     * @param id the id to search for
     * @return the Errata found
     */
    public static Errata lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(Errata.class, id);
    }

    /**
     * Lookup a Errata by the advisoryType string
     * @param advisoryType to search for
     * @return the Errata found
     */
    public static List<Errata> lookupErratasByAdvisoryType(String advisoryType) {
        List<Errata> retval;
        try {
            retval = getSession().createQuery("FROM Errata AS e WHERE e.advisoryType = :type", Errata.class)
                    .setParameter("type", advisoryType, StandardBasicTypes.STRING)
                    //Retrieve from cache if there
                    .setCacheable(true)
                    .list();
        }
        catch (HibernateException he) {
            log.error("Error loading ActionArchTypes from DB", he);
            throw new HibernateRuntimeException("Error loading ActionArchTypes from db");
        }
        return retval;
    }

    /**
     * Finds errata by id
     * @param id errata id
     * @return Errata if found, otherwise null
     */
    public static Errata lookupErrataById(Long id) {
        Errata retval;
        try {
            retval = getSession().createQuery("FROM Errata AS e WHERE e.id = :id", Errata.class)
                    .setParameter("id", id, StandardBasicTypes.LONG)
                    .uniqueResult();
        }
        catch (HibernateException he) {
            log.error("Error loading Errata from DB", he);
            throw new HibernateRuntimeException("Error loading Errata from db");
        }
        return retval;
    }

    /**
     * Retrieves the errata that belongs to a vendor or a given organization, given an advisory name.
     * @param advisory The advisory to lookup
     * @param org the organization
     * @return Returns the errata corresponding to the passed in advisory name.
     */
    public static List<Errata> lookupVendorAndUserErrataByAdvisoryAndOrg(String advisory, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM Errata AS e
                        WHERE e.advisoryName = :advisory AND (e.org = :org OR e.org is null)
                        """, Errata.class)
                .setParameter("advisory", advisory, StandardBasicTypes.STRING)
                .setParameter("org", org)
                .getResultList();
    }

    /**
     * Retrieves the errata that with the given advisory and a given Org.
     * @param advisory The advisory to lookup
     * @param org User organization
     * @return Returns the errata corresponding to the passed in advisory name.
     */
    public static Errata lookupByAdvisoryAndOrg(String advisory, Org org) {
        return HibernateFactory.getSession()
                .createQuery("""
                       FROM Errata AS e
                       WHERE e.advisoryName = :advisory
                       AND ((:org is NOT null AND e.org = :org) OR (:org is null AND e.org is null))
                       """, Errata.class)
                .setParameter("advisory", advisory, StandardBasicTypes.STRING)
                .setParameter("org", org)
                .uniqueResult();
    }

    /**
     * Retrieves the errata that belongs to a vendor or a given organization, given an advisory id.
     * @param advisoryId errata advisory id
     * @param org User organization
     * @return Errata if found, otherwise null
     */
    public static List<Errata> lookupByAdvisoryId(String advisoryId, Org org) {
        List<Errata> retval;
        try {
            retval = getSession().createQuery("""
                            FROM Errata AS e
                            WHERE e.advisory = :advisory AND (e.org = :org OR e.org is null)
                            """, Errata.class)
                    .setParameter("advisory", advisoryId, StandardBasicTypes.STRING)
                    .setParameter("org", org)
                    .getResultList();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by advisory name");
        }
        return retval;
    }

    /**
     * Finds errata based on CVE string
     * @param cve cve text
     * @return Errata if found, otherwise null
     */
    public static List<Errata> lookupByCVE(String cve) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES, "erratas_for_cve");
        Map<String, Object> params = new HashMap<>();
        params.put("cve", cve);
        DataResult<Map<String, Object>> result = mode.execute(params);
        Session session = HibernateFactory.getSession();
        return result.stream()
                .map(row -> session.load(Errata.class, (Long) row.get("id")))
                .collect(Collectors.toList());
    }

    /**
     * Lookup all the clones of a particular errata
     * @param org Org that the clones belongs to
     * @param original Original errata that the clones are clones of
     * @return list of clones of the errata
     */
    public static List<Errata> lookupByOriginal(Org org, Errata original) {
        List<Errata> retval;

        try {
            retval = lookupErrataByOriginal(org, original);
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by original errata");
        }
        return retval;
    }

    /**
     * Lookup all the clones of a particular errata
     * @param org Org that the clones belongs to
     * @param original Original errata that the clones are clones of
     * @return list of clones of the errata
     */
    public static List<Errata> lookupErrataByOriginal(Org org, Errata original) {
        List<Errata> retval;

        try {
            retval = getSession().createQuery("""
                            FROM ClonedErrata AS c
                            WHERE c.original = :original
                            AND c.org = :org
                            """, Errata.class)
                    .setParameter("original", original)
                    .setParameter("org", org)
                    .list();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by original errata");
        }
        return retval;
    }

    /**
     * Lists errata present in both channels
     * @param channelFrom channel1
     * @param channelTo channel2
     * @return list of errata
     */
    public static List<Errata> listErrataInBothChannels(Channel channelFrom, Channel channelTo) {
        List<Errata> retval;

        try {
            retval = getSession().createQuery("""
                            FROM Errata AS e
                            WHERE :channel_from IN elements(e.channels)
                            AND :channel_to IN elements(e.channels)
                            """, Errata.class)
                    .setParameter("channel_from", channelFrom)
                    .setParameter("channel_to", channelTo)
                    .list();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by original errata");
        }
        return retval;
    }

    /**
     * Lists errata from channelFrom, that are cloned from the same original
     * as errata in channelTo
     * @param channelFrom channel1
     * @param channelTo channel2
     * @return list of errata
     */
    public static List<Errata> listSiblingsInChannels(Channel channelFrom, Channel channelTo) {
        List<Errata> retval;

        try {
            retval = getSession().createQuery("""
                        SELECT e_from FROM ClonedErrata AS e_from, ClonedErrata AS e_to
                        WHERE :channel_from IN elements(e_from.channels)
                        AND e_from.original = e_to.original
                        AND :channel_to IN elements(e_to.channels)
                        """, Errata.class)
                    .setParameter("channel_from", channelFrom)
                    .setParameter("channel_to", channelTo)
                    .list();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by original errata");
        }
        return retval;
    }

    /**
     * Lists errata from channelFrom, that have clones in channelTo
     * @param channelFrom channel1
     * @param channelTo channel2
     * @return list of errata
     */
    public static List<Errata> listClonesInChannels(Channel channelFrom, Channel channelTo) {
        List<Errata> retval;

        try {
            retval = getSession().createQuery("""
                             SELECT e
                             FROM Errata AS e, ClonedErrata AS c
                             WHERE :channel_from IN elements(e.channels)
                             AND c.original = e
                             AND :channel_to IN elements(c.channels)
                            """, Errata.class)
                    .setParameter("channel_from", channelFrom)
                    .setParameter("channel_to", channelTo)
                    .list();
        }
        catch (HibernateException e) {
            throw new HibernateRuntimeException("Error looking up errata by original errata");
        }
        return retval;
    }

    /**
     * Insert or Update a Errata.
     * @param errataIn Errata to be stored in database.
     */
    public static void save(Errata errataIn) {
        singleton.saveObject(errataIn);
    }

    /**
     * Delete a bug
     * @param deleteme Bug to delete
     */
    public static void removeBug(Bug deleteme) {
        singleton.removeObject(deleteme);
    }

    /**
     * Delete a Keyword
     * @param deleteme Keyword to delete
     */
    public static void remove(Keyword deleteme) {
        singleton.removeObject(deleteme);
    }


    /**
     * Remove a file.
     * @param deleteme ErrataFile to delete
     */
    public static void removeFile(ErrataFile deleteme) {
        singleton.removeObject(deleteme);
    }

    /**
     * Lists errata assigned to a particular channel.
     *
     * @param org the Org in question
     * @param channel the channel you want to get the errata for
     * @return A list of Errata objects
     */
    public static List<Errata> listByChannel(Org org, Channel channel) {
        return HibernateFactory.getSession().
                createQuery("""
                        FROM Errata e
                        WHERE :channel member of e.channels
                        AND (e.org = :org OR e.org is null)
                        """, Errata.class)
                .setParameter("org", org)
                .setParameter("channel", channel)
                .list();
    }

    /**
     * Lists errata assigned to a particular channel between
     * the given start and end date. The list is sorted by date
     * (from oldest to newest).
     * @param org the Org in question
     * @param channel the channel you want to get the errata for
     * @param startDate the start date
     * @param endDate the end date
     * @return A list of Errata objects
     */
    public static List<Errata> lookupByChannelBetweenDates(Org org, Channel channel, String startDate, String endDate) {

        return HibernateFactory.getSession().
                createQuery("""
                        FROM Errata AS e
                        WHERE :channel IN elements(e.channels)
                        AND (e.org = :org OR e.org is null)
                        AND (e.lastModified > to_timestamp(:start_date, 'YYYY-MM-DD HH24:MI:SS'))
                        AND (e.lastModified < to_timestamp(:end_date, 'YYYY-MM-DD HH24:MI:SS'))
                        ORDER BY e.issueDate
                        """, Errata.class)
                .setParameter("org", org)
                .setParameter("channel", channel)
                .setParameter("start_date", startDate, StandardBasicTypes.STRING)
                .setParameter("end_date", endDate, StandardBasicTypes.STRING)
                .list();
    }

    /**
     * Lookup an errataFile object by its errata id and package filename.
     * @param errataId the ID of the errata associated
     * @param filename the filename of the package associated
     * @return an Optional that may or may not contain the requested errata file object
     */
    public static Optional<ErrataFile> lookupErrataFile(Long errataId, String filename) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ErrataFile as pef
                        WHERE pef.owningErrata.id = :errata_id
                        AND pef.fileName = :filename""", ErrataFile.class)
                .setParameter("errata_id", errataId, StandardBasicTypes.LONG)
                .setParameter("filename", filename, StandardBasicTypes.STRING)
                .uniqueResultOptional();
    }

    /**
     * Takes a set of packages that should be installed on a set of systems and checks whether there are
     * and packages that are retracted. A packages counts as retracted for a server if it is contained
     * in any retracted errata of a channel assigned to the system.
     *
     * @param pids package ids
     * @param sids server ids
     * @return pairs of package and server ids of packages that are retracted for a given server.
     */
    public static List<Tuple2<Long, Long>> retractedPackages(List<Long> pids, List<Long> sids) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                                SELECT pid, sid
                                FROM suseServerChannelsRetractedPackagesView
                                WHERE pid IN (:pids) AND sid IN (:sids)
                                """,
                        Tuple.class)
                .addScalar("pid", StandardBasicTypes.LONG)
                .addScalar("sid", StandardBasicTypes.LONG)
                .setParameter("pids", pids)
                .setParameter("sids", sids)
                .stream()
                .map(r -> new Tuple2<>(r.get(0, Long.class), r.get(1, Long.class)))
                .collect(Collectors.toList());
    }

    /**
     * Takes a set of packages that should be installed on a set of systems and checks whether there are
     * and packages that are retracted. A packages counts as retracted for a server if it is contained
     * in any retracted errata of a channel assigned to the system.
     *
     * @param nevras package nevras as formatted string
     * @param sids server ids
     * @return pairs of package and server ids of packages that are retracted for a given server.
     */
    public static List<Tuple2<Long, Long>> retractedPackagesByNevra(List<String> nevras, List<Long> sids) {
        if (nevras.isEmpty()) {
            return new LinkedList<>();
        }
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                                SELECT pid, sid
                                FROM suseServerChannelsRetractedPackagesView
                                JOIN rhnpackage p on p.id = pid
                                JOIN rhnpackagename pn on pn.id = p.name_id
                                JOIN rhnpackageevr pevr on pevr.id = p.evr_id
                                JOIN rhnpackagearch parch on parch.id = p.package_arch_id
                                WHERE (pn.name || '-' || COALESCE(pevr.epoch || ':', '') ||
                                        pevr.version || '-' || pevr.release || '.' || parch.label) IN (:nevras)
                                AND sid IN (:sids)
                                """,
                        Tuple.class)
                .addScalar("pid", StandardBasicTypes.LONG)
                .addScalar("sid", StandardBasicTypes.LONG)
                .setParameter("nevras", nevras)
                .setParameter("sids", sids)
                .stream()
                .map(r -> new Tuple2<>(r.get(0, Long.class), r.get(1, Long.class)))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of ErrataOverview that match the given errata ids.
     * @param eids Errata ids.
     * @param org Organization to match results with
     * @return a list of ErrataOverview that match the given errata ids.
     */
    public static List<ErrataOverview> search(List<Long> eids, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                                SELECT DISTINCT e.id, e.advisory,
                                e.advisory_name AS advisoryName,
                                e.advisory_type AS advisoryType,
                                e.synopsis AS advisorySynopsis,
                                e.advisory_status AS advisoryStatus,
                                e.update_date AS updateDate,
                                e.issue_date AS issueDate,
                                CASE WHEN rb.keyword IS NOT NULL THEN 1 ELSE 0 END AS reboot_suggested,
                                CASE WHEN rs.keyword IS NOT NULL THEN 1 ELSE 0 END AS restart_suggested,
                                e.severity_id AS severityId
                                FROM rhnErrata e
                                    LEFT JOIN rhnerratakeyword rb
                                        ON e.id = rb.errata_id AND rb.keyword = 'reboot_suggested'
                                    LEFT JOIN rhnerratakeyword rs
                                        ON e.id = rs.errata_id AND rs.keyword = 'restart_suggested',
                                rhnChannelErrata CE
                                WHERE e.id IN (:eids)
                                AND CE.errata_id = e.id
                                AND CE.channel_id IN
                                    (SELECT channel_id FROM rhnAvailableChannels WHERE org_id = :org_id)
                                """,
                        Tuple.class)
                .addScalar("id", StandardBasicTypes.LONG) //0
                .addScalar("advisory", StandardBasicTypes.STRING) //1
                .addScalar("advisoryName", StandardBasicTypes.STRING) //2
                .addScalar("advisoryType", StandardBasicTypes.STRING) //3
                .addScalar("advisoryStatus", StandardBasicTypes.STRING) //4
                .addScalar("advisorySynopsis", StandardBasicTypes.STRING) //5
                .addScalar("updateDate", StandardBasicTypes.DATE) //6
                .addScalar("issueDate", StandardBasicTypes.DATE) //7
                .addScalar("reboot_suggested", StandardBasicTypes.BOOLEAN) //8
                .addScalar("restart_suggested", StandardBasicTypes.BOOLEAN) //9
                .addScalar("severityId", StandardBasicTypes.INTEGER) //10
                .setParameter("eids", eids)
                .setParameter("org_id", org.getId())
                .stream()
                .map(r -> {
                    ErrataOverview eo = new ErrataOverview();
                    // e.id, e.advisory, e.advisoryName, e.advisoryType, e.synopsis, e.updateDate
                    eo.setId(r.get(0, Long.class));
                    eo.setAdvisory(r.get(1, String.class));
                    eo.setAdvisoryName(r.get(2, String.class));
                    eo.setAdvisoryType(r.get(3, String.class));
                    eo.setAdvisoryStatus(AdvisoryStatus.fromMetadata(r.get(4, String.class)).orElseThrow());
                    eo.setAdvisorySynopsis(r.get(5, String.class));
                    eo.setUpdateDate(r.get(6, Date.class));
                    eo.setIssueDate(r.get(7, Date.class));
                    eo.setRebootSuggested(r.get(8, Boolean.class));
                    eo.setRestartSuggested(r.get(9, Boolean.class));
                    eo.setSeverityid(r.get(10, Integer.class));
                    return eo;
                })
                .collect(Collectors.toList());

    }

    /**
     * Returns a list of ErrataOverview of Errata that match the given Package
     * ids.
     * @param pids Package ids whose Errata are being sought.
     * @return a list of ErrataOverview of Errata that match the given Package
     * ids.
     */
    public static List<ErrataOverview> searchByPackageIds(List<Long> pids) {
        log.debug("pids = {}", pids);

        Session session = HibernateFactory.getSession();
        List<Tuple> results = session.createNativeQuery("""
                                SELECT DISTINCT e.id, e.advisory, e.advisory_name AS advisoryName,
                                e.advisory_type AS advisoryType,
                                e.synopsis AS advisorySynopsis,
                                e.advisory_status AS advisoryStatus,
                                e.update_date AS updateDate,
                                e.issue_date AS issueDate,
                                pn.name,
                                CASE WHEN rb.keyword IS NOT NULL THEN 1 ELSE 0 END AS reboot_suggested,
                                CASE WHEN rs.keyword IS NOT NULL THEN 1 ELSE 0 END AS restart_suggested,
                                p.id AS pid
                                FROM rhnErrata e
                                    LEFT JOIN rhnerratakeyword rb
                                        ON e.id = rb.errata_id AND rb.keyword = 'reboot_suggested'
                                    LEFT JOIN rhnerratakeyword rs
                                        ON e.id = rs.errata_id AND rs.keyword = 'restart_suggested',
                                rhnErrataPackage ep,
                                rhnPackage p,
                                rhnPackageName pn
                                WHERE e.id = ep.errata_id
                                AND p.id = ep.package_id
                                AND p.name_id = pn.id
                                AND ep.package_id IN (:pids)
                                ORDER BY e.id
                                """,
                        Tuple.class)
                .addScalar("id", StandardBasicTypes.LONG) //0
                .addScalar("advisory", StandardBasicTypes.STRING) //1
                .addScalar("advisoryName", StandardBasicTypes.STRING) //2
                .addScalar("advisoryType", StandardBasicTypes.STRING) //3
                .addScalar("advisoryStatus", StandardBasicTypes.STRING) //4
                .addScalar("advisorySynopsis", StandardBasicTypes.STRING) //5
                .addScalar("updateDate", StandardBasicTypes.DATE) //6
                .addScalar("issueDate", StandardBasicTypes.DATE) //7
                .addScalar("name", StandardBasicTypes.STRING) //8
                .addScalar("reboot_suggested", StandardBasicTypes.BOOLEAN) //9
                .addScalar("restart_suggested", StandardBasicTypes.BOOLEAN) //10
                .addScalar("pid", StandardBasicTypes.LONG) //11
                .setParameter("pids", pids)
                .list();

        log.debug("ErrataFactory.searchByPackageIds returned {} entries", results.size());

        return convertTupleInErrataOverview(results);
    }

    private static List<ErrataOverview> convertTupleInErrataOverview(List<Tuple> results) {

        Long lastId = null;
        ErrataOverview eo = null;
        List<ErrataOverview> errataOverviewList = new ArrayList<>();

        for (Tuple r : results) {
            // e.id, e.advisory, e.advisoryName, e.advisoryType, e.synopsis, e.updateDate
            Long curId = r.get(0, Long.class);

            if (!curId.equals(lastId)) {
                eo = new ErrataOverview();
            }
            eo.setId(r.get(0, Long.class));
            eo.setAdvisory(r.get(1, String.class));
            eo.setAdvisoryName(r.get(2, String.class));
            eo.setAdvisoryType(r.get(3, String.class));
            eo.setAdvisoryStatus(AdvisoryStatus.fromMetadata(r.get(4, String.class)).orElseThrow());
            eo.setAdvisorySynopsis(r.get(5, String.class));
            eo.setUpdateDate(r.get(6, Date.class));
            eo.setIssueDate(r.get(7, Date.class));
            eo.addPackageName(r.get(8, String.class));
            eo.setRebootSuggested(r.get(9, Boolean.class));
            eo.setRestartSuggested(r.get(10, Boolean.class));
            eo.addPackageId(r.get(11, Long.class));
            if (!curId.equals(lastId)) {
                errataOverviewList.add(eo);
                lastId = curId;
            }
            log.debug("curId = {}, lastId = {}", curId, lastId);
            log.debug("ErrataOverview formed: {} for {}", eo.getAdvisoryName(), eo.getPackageNames());
        }

        return errataOverviewList;
    }

    /**
     * Returns a list of ErrataOverview of Errata that match the given Package
     * ids.
     * @param pids Package ids whose Errata are being sought.
     * @param org Organization to match results with
     * @return a list of ErrataOverview of Errata that match the given Package
     * ids.
     */
    public static List<ErrataOverview> searchByPackageIdsWithOrg(List<Long> pids, Org org) {
        log.debug("org_id = {}, pids = {}", org.getId(), pids);

        Session session = HibernateFactory.getSession();
        List<Tuple> results = session.createNativeQuery("""
                                SELECT DISTINCT e.id, e.advisory, e.advisory_name AS advisoryName,
                                e.advisory_type AS advisoryType,
                                e.synopsis AS advisorySynopsis,
                                e.advisory_status AS advisoryStatus,
                                e.update_date AS updateDate,
                                e.issue_date AS issueDate,
                                pn.name,
                                CASE WHEN rb.keyword IS NOT NULL THEN 1 ELSE 0 END AS reboot_suggested,
                                CASE WHEN rs.keyword IS NOT NULL THEN 1 ELSE 0 END AS restart_suggested,
                                p.id AS pid
                                FROM rhnErrata e
                                    LEFT JOIN rhnerratakeyword rb
                                        ON e.id = rb.errata_id AND rb.keyword = 'reboot_suggested'
                                    LEFT JOIN rhnerratakeyword rs
                                        ON e.id = rs.errata_id AND rs.keyword = 'restart_suggested',
                                rhnErrataPackage ep,
                                rhnPackage p,
                                rhnPackageName pn,
                                rhnAvailableChannels ac,
                                rhnChannelErrata ce
                                WHERE e.id = ep.errata_id
                                AND p.id = ep.package_id
                                AND p.name_id = pn.id
                                AND ep.package_id IN (:pids)
                                AND e.id = ce.errata_id
                                AND ce.channel_id = ac.channel_id
                                AND ac.org_id = :org_id
                                ORDER BY e.id
                                """,
                        Tuple.class)
                .addScalar("id", StandardBasicTypes.LONG) //0
                .addScalar("advisory", StandardBasicTypes.STRING) //1
                .addScalar("advisoryName", StandardBasicTypes.STRING) //2
                .addScalar("advisoryType", StandardBasicTypes.STRING) //3
                .addScalar("advisoryStatus", StandardBasicTypes.STRING) //4
                .addScalar("advisorySynopsis", StandardBasicTypes.STRING) //5
                .addScalar("updateDate", StandardBasicTypes.DATE) //6
                .addScalar("issueDate", StandardBasicTypes.DATE) //7
                .addScalar("name", StandardBasicTypes.STRING) //8
                .addScalar("reboot_suggested", StandardBasicTypes.BOOLEAN) //9
                .addScalar("restart_suggested", StandardBasicTypes.BOOLEAN) //10
                .addScalar("pid", StandardBasicTypes.LONG) //11
                .setParameter("pids", pids)
                .setParameter("org_id", org.getId())
                .list();

        log.debug("ErrataFactory.searchByPackageIdsWithOrg returned {} entries", results.size());

        return convertTupleInErrataOverview(results);
    }


    /**
     * Sync all the errata details from one errata to another
     * @param cloned the cloned errata that needs syncing
     */
    public static void syncErrataDetails(ClonedErrata cloned) {
        Errata original = cloned.getOriginal();

        //Set the easy things first ;)
        cloned.setAdvisoryType(original.getAdvisoryType());
        cloned.setProduct(original.getProduct());
        cloned.setErrataFrom(original.getErrataFrom());
        cloned.setDescription(original.getDescription());
        cloned.setSynopsis(original.getSynopsis());
        cloned.setTopic(original.getTopic());
        cloned.setSolution(original.getSolution());
        cloned.setIssueDate(original.getIssueDate());
        cloned.setUpdateDate(original.getUpdateDate());
        cloned.setNotes(original.getNotes());
        cloned.setRefersTo(original.getRefersTo());
        cloned.setAdvisoryRel(original.getAdvisoryRel());
        cloned.setLocallyModified(original.getLocallyModified());
        cloned.setLastModified(original.getLastModified());
        cloned.setSeverity(original.getSeverity());
        AdvisoryStatus previousAdvisoryStatus = cloned.getAdvisoryStatus();
        cloned.setAdvisoryStatus(original.getAdvisoryStatus());

        // Copy the packages
        cloned.setPackages(new HashSet<>(original.getPackages()));

        // Copy the keywords
        original.getKeywords().forEach(k -> cloned.addKeyword(k));

        // Copy the bugs
        original.getBugs().stream()
                .map(bug -> createBug(bug.getId(), bug.getSummary(), bug.getUrl()))
                .forEach(bug -> cloned.addBug(bug));

        // only update the cache if exactly one of patches is retracted
        if (previousAdvisoryStatus != cloned.getAdvisoryStatus() &&
                (previousAdvisoryStatus == RETRACTED || cloned.getAdvisoryStatus() == RETRACTED)) {
            boolean retract = (cloned.getAdvisoryStatus() == RETRACTED);
            cloned.getChannels().forEach(c -> {
                processRetracted(cloned.getId(), c.getId(), retract);
                ChannelFactory.refreshNewestPackageCache(c, "sync errata");
                ChannelManager.queueChannelChange(c.getLabel(), "java::syncErrata", "Errata synced");
            });
        }
    }

    private static void processRetracted(long errataId, long channelId, boolean retract) {
        List<Long> erratumPids = ErrataFactory.listErrataChannelPackages(channelId, errataId);
        if (retract) {
            ErrataCacheManager.deleteCacheEntriesForChannelErrata(channelId, List.of(errataId));
            ErrataCacheManager.deleteCacheEntriesForChannelPackages(channelId, erratumPids);
        }
        else {
            ErrataCacheManager.insertCacheForChannelPackages(channelId, errataId, erratumPids);
        }
    }

    /**
     * List errata objects by id and org
     * @param ids list of ids
     * @param orgId the organization id
     * @return List of Errata Objects
     */
    public static List<Errata> listErrata(Collection<Long> ids, Long orgId) {
        return getSession().createNativeQuery("""
                        SELECT e.*, ec.original_id, CASE WHEN ec.original_id IS NULL THEN 0 ELSE 1 END AS clazz_
                        FROM rhnErrata AS e
                        LEFT JOIN rhnErrataCloned ec ON e.id = ec.id
                        WHERE e.id IN (:eids)
                        AND (e.org_id = :orgId
                            OR EXISTS (SELECT 1 FROM WEB_CUSTOMER org
                                        INNER JOIN rhnTrustedOrgs torg ON org.id = torg.org_id
                                        WHERE org.id = :orgId AND e.org_id = torg.org_trust_id)
                            OR EXISTS (SELECT 1 FROM rhnAvailableChannels ac, rhnChannelErrata ce
                                        WHERE ce.errata_id = e.id
                                        AND ce.channel_id = ac.channel_id
                                        AND ac.org_id = :orgId))
                        """, Tuple.class)
                .addEntity("e", Errata.class)
                .setParameter("orgId", orgId)
                .setParameterList("eids", ids)
                .stream()
                .map(t -> t.get(0, Errata.class))
                .collect(Collectors.toList());
    }

    /**
     * Get list of errata ids that are in one channel but not another
     * @param fromCid errata are in this channel
     * @param toCid but not in this one
     * @return list of errata overviews
     */
    public static DataResult<ErrataOverview> relevantToOneChannelButNotAnother(Long fromCid, Long toCid) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES,
                "relevant_to_one_channel_but_not_another");
        Map<String, Object> params = new HashMap<>();
        params.put("from_cid", fromCid);
        params.put("to_cid", toCid);
        return mode.execute(params);
    }

    /**
     * List all owned, unmodified, cloned errata in an org. Useful when cloning
     * channels.
     * @param orgId Org id to look for
     * @return List of OwnedErrata
     */
    public static DataResult<OwnedErrata> listOwnedUnmodifiedClonedErrata(Long orgId) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES,
                "owned_unmodified_cloned_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        return mode.execute(params);
    }

    /**
     * Get all advisory strings that end in the given string.
     * Useful when cloning errata.
     * @param ending String ending of the advisory
     * @return Set of existing advisories
     */
    public static Set<String> listAdvisoriesEndingWith(String ending) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES, "advisories_ending_with");
        Map<String, Object> params = new HashMap<>();
        params.put("ending", "%" + ending);
        List<Map<String, Object>> results = mode.execute(params);
        Set<String> ret = new HashSet<>();
        for (Map<String, Object> result : results) {
            ret.add((String) result.get("advisory"));
        }
        return ret;
    }

    /**
     * Get all advisory names that end in the given string.
     * Useful when cloning errata.
     * @param ending String ending of the advisory
     * @return Set of existing advisory names
     */
    public static Set<String> listAdvisoryNamesEndingWith(String ending) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES,
                "advisory_names_ending_with");
        Map<String, Object> params = new HashMap<>();
        params.put("ending", "%" + ending);
        List<Map<String, Object>> results = mode.execute(params);
        Set<String> ret = new HashSet<>();
        for (Map<String, Object> result : results) {
            ret.add((String) result.get("advisory_name"));
        }
        return ret;
    }

    /**
     * Get ErrataOverview by errata id
     * @param eid errata id
     * @return ErrataOverview object
     */
    public static ErrataOverview getOverviewById(Long eid) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES, "overview_by_id");
        Map<String, Object> params = new HashMap<>();
        params.put("eid", eid);
        DataResult<ErrataOverview> results = mode.execute(params);
        if (results.isEmpty()) {
            return null;
        }
        results.elaborate();
        return results.get(0);
    }

    /**
     * Get ErrataOverview by advisory
     * @param advisory the advisory
     * @return ErrataOverview object
     */
    public static ErrataOverview getOverviewByAdvisory(String advisory) {
        SelectMode mode = ModeFactory.getMode(ERRATA_QUERIES, "overview_by_advisory");
        Map<String, Object> params = new HashMap<>();
        params.put("advisory", advisory);
        DataResult<ErrataOverview> results = mode.execute(params);
        if (results.isEmpty()) {
            return null;
        }
        results.elaborate();
        return results.get(0);
    }

    /**
     * Clone an erratum in the db. Will fill contents of rhnErrata, rhnErrataCloned,
     * rhnErrataBugList, rhnErrataPackage, rhnErrataKeyword, and rhnErrataCVE. Basically
     * do everything that ErrataHelper.cloneErrataFast does, but much, much faster.
     * @param originalEid erratum id to clone from
     * @param advisory unique advisory
     * @param advisoryName unique name
     * @param orgId org id to clone into
     * @return ErrataOverview for the cloned erratum
     */
    public static ErrataOverview cloneErratum(Long originalEid, String advisory, String advisoryName, Long orgId) {
        WriteMode m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_erratum");
        Map<String, Object> params = new HashMap<>();
        params.put("eid", originalEid);
        params.put("advisory", advisory);
        params.put("name", advisoryName);
        params.put("org_id", orgId);
        m.executeUpdate(params);
        ErrataOverview clone = getOverviewByAdvisory(advisory);

        // set original
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "set_original");
        params = new HashMap<>();
        params.put("original_id", originalEid);
        params.put("clone_id", clone.getId());
        m.executeUpdate(params);

        // clone bugs
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_bugs");
        m.executeUpdate(params);

        // clone keywords
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_keywords");
        m.executeUpdate(params);

        // clone packages
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_packages");
        m.executeUpdate(params);

        // clone cves
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_cves");
        m.executeUpdate(params);

        // clone files
        m = ModeFactory.getWriteMode(ERRATA_QUERIES, "clone_files");
        m.executeUpdate(params);

        return clone;
    }

}

