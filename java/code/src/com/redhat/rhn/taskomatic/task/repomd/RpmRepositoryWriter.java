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
package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.RepoMetadata;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.manager.task.TaskManager;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @version $Rev $
 *
 */
public class RpmRepositoryWriter extends RepositoryWriter {

    private static final String PRIMARY_FILE = "primary.xml.gz.new";
    private static final String FILELISTS_FILE = "filelists.xml.gz.new";
    private static final String OTHER_FILE = "other.xml.gz.new";
    private static final String REPOMD_FILE = "repomd.xml.new";
    private static final String UPDATEINFO_FILE = "updateinfo.xml.gz.new";
    private static final String PRODUCTS_FILE = "products.xml";
    private static final String SUSEDATA_FILE = "susedata.xml.gz.new";
    private static final String NOREPO_FILE = "noyumrepo.txt";
    private static final String SOLV_FILE = "solv.new";
    private static final String REPO2SOLV = "/usr/bin/repo2solv";

    private static final String GROUP = "groups";
    private static final String MODULES = "modules";

    /**
     * Utility class to move/copy files around in the 'repodata' directory
     */
    private static class RepomdFileOrganizer {
        private String directory;
        private long lastModificationTime;

        /**
         * Initialize an instance with a base directory and a predefined last modification time
         *
         * @param directoryIn the base directory for the repo metadata
         * @param lastModificationTimeIn the last modification time to be set on the processed files
         */
        private RepomdFileOrganizer(String directoryIn, long lastModificationTimeIn) {
            this.directory = directoryIn;
            this.lastModificationTime = lastModificationTimeIn;
        }

        /**
         * Move a temp file to its final destination, both inside the base directory
         *
         * @param tempFilename the temp filename in the directory
         * @param finalFilename the final filename in the directory
         * @return the final file object
         */
        public File move(String tempFilename, String finalFilename) {
            return move(tempFilename, finalFilename, null);
        }

        public File move(String tempFilename, String finalFilename, String checksum) {
            File tempFile = new File(directory, tempFilename);
            File finalFile;
            if (checksum != null) {
                finalFile = new File(directory, checksum + "-" + finalFilename);
            }
            else {
                finalFile = new File(directory, finalFilename);
            }

            tempFile.renameTo(finalFile);
            finalFile.setLastModified(lastModificationTime);
            return finalFile;
        }

        /**
         * Copy a source file from anywhere in the filesystem into the base directory
         *
         * @param sourceFilepath the absolute source path
         * @param finalFilename the final filename
         * @param checksum the checksum to be prepended to the filename
         * @return the final file object
         */
        public File copy(String sourceFilepath, String finalFilename, String checksum) {
            File sourceFile = new File(sourceFilepath);
            File finalFile;
            if (checksum != null) {
                finalFile = new File(directory, checksum + "-" + finalFilename);
            }
            else {
                finalFile = new File(directory, finalFilename);
            }

            try {
                FileUtils.copyFile(sourceFile, finalFile);
            }
            catch (IOException e) {
                throw new RuntimeException(MessageFormat.format("Cannot copy '{0}' file, cancelling",
                        finalFilename), e);
            }

            finalFile.setLastModified(lastModificationTime);
            return finalFile;
        }
    }

    /**
     * Constructor takes in pathprefix and mountpoint
     * @param pathPrefixIn prefix to package path
     * @param mountPointIn mount point package resides
     */
    public RpmRepositoryWriter(String pathPrefixIn, String mountPointIn) {
        this(pathPrefixIn, mountPointIn, new SystemCommandExecutor());
    }

    /**
     * Constructor takes in pathprefix and mountpoint
     * @param pathPrefixIn prefix to package path
     * @param mountPointIn mount point package resides
     * @param cmdExecutorIn {@link Executor} instance to run system commands
     */
    public RpmRepositoryWriter(String pathPrefixIn, String mountPointIn, Executor cmdExecutorIn) {
        super(pathPrefixIn, mountPointIn, cmdExecutorIn);
    }

    /**
     *
     * @param channel channel info
     * @return repodata sanity
     */
    @Override
    public boolean isChannelRepodataStale(Channel channel) {
        File theFile = new File(mountPoint + File.separator + pathPrefix +
                File.separator + channel.getLabel() + File.separator +
                "repomd.xml");
        Date mdlastModified = new Date(theFile.lastModified());
        Date dblastModified = channel.getLastModified();
        log.info("File Modified Date:" + LocalizationService.getInstance().
                formatCustomDate(mdlastModified));
        log.info("Channel Modified Date:" + LocalizationService.getInstance().
                formatCustomDate(dblastModified));
        // We need to cut some digits from ms, we don't want to be very accurate. However
        // removing ms completely will not work either.
        Long mdfasttimeCut = mdlastModified.getTime() / 100;
        Long dbfasttimeCut = dblastModified.getTime() / 100;

        return !mdfasttimeCut.equals(dbfasttimeCut);

    }

    /**
     * Initialize the base directory by creating the directories as needed
     *
     * @param dirPath the path to the base directory
     * @return the list of files existing in the directory, if any
     */
    private List<File> initBaseDir(String dirPath) {
        File baseDir = new File(dirPath);
        if (baseDir.mkdirs()) {
            return Collections.emptyList();
        }
        else if (baseDir.exists() && baseDir.isDirectory()) {
            return Arrays.asList(baseDir.listFiles());
        }
        else {
            throw new RepomdRuntimeException("Unable to create directory: " + dirPath);
        }
    }

    /**
     *
     * @param channel channelinfo for repomd file creation
     */
    @Override
    public void writeRepomdFiles(Channel channel) {
        PackageManager.createRepoEntrys(channel.getId());

        // we closed the session, so we need to reload the object
        channel = HibernateFactory.getSession().get(channel.getClass(), channel.getId());

        // Initialize the directory, and keep a list of already existing files in the directory, if any
        String prefix = mountPoint + File.separator + pathPrefix + File.separator + channel.getLabel() + File.separator;
        List<File> existingFiles = initBaseDir(prefix);

        // Get compatible checksumType
        String checksumType = channel.getChecksumTypeLabel();
        if (checksumType == null) {
            generateBadRepo(channel, prefix);
            return;
        }
        new File(prefix + NOREPO_FILE).delete();
        if (log.isDebugEnabled()) {
            log.debug("Checksum Type Value: " + checksumType);
        }

        // java.security.MessageDigest recognizes:
        // MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512
        String checksumAlgo = checksumType;
        if (checksumAlgo.toUpperCase().startsWith("SHA")) {
            checksumAlgo = checksumType.substring(0, 3) + "-" +
                    checksumType.substring(3);
        }
        // translate sha1 to sha for xml repo files
        String checksumLabel = checksumType;
        if (checksumLabel.equals("sha1")) {
            checksumLabel = "sha";
        }

        log.info("Generating new repository metadata for channel '" +
                channel.getLabel() + "'(" + checksumType + ") " +
                channel.getPackageCount() + " packages, " +
                channel.getErrataCount() + " errata");

        CompressingDigestOutputWriter primaryFile, filelistsFile, otherFile, susedataFile;

        try {
            primaryFile = new CompressingDigestOutputWriter(
                    new FileOutputStream(prefix + PRIMARY_FILE),
                    checksumAlgo);
            filelistsFile = new CompressingDigestOutputWriter(
                    new FileOutputStream(prefix + FILELISTS_FILE),
                    checksumAlgo);
            otherFile = new CompressingDigestOutputWriter(
                    new FileOutputStream(prefix + OTHER_FILE), checksumAlgo);
            susedataFile = new CompressingDigestOutputWriter(
                    new FileOutputStream(prefix + SUSEDATA_FILE), checksumAlgo);
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new RepomdRuntimeException(e);
        }

        BufferedWriter primaryBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(primaryFile));
        BufferedWriter filelistsBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(filelistsFile));
        BufferedWriter otherBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(otherFile));
        BufferedWriter susedataBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(susedataFile));
        PrimaryXmlWriter primary = new PrimaryXmlWriter(
                primaryBufferedWriter);
        FilelistsXmlWriter filelists = new FilelistsXmlWriter(
                filelistsBufferedWriter);
        OtherXmlWriter other = new OtherXmlWriter(otherBufferedWriter);
        SuseDataXmlWriter susedata = new SuseDataXmlWriter(
                susedataBufferedWriter);
        Date start = new Date();

        primary.begin(channel);
        filelists.begin(channel);
        other.begin(channel);
        susedata.begin(channel);

        // batch the elaboration so we don't have to hold many thousands of packages in memory at once
        final int batchSize = 1000;
        for (long i = 0; i < channel.getPackageCount(); i += batchSize) {
            DataResult<PackageDto> packageBatch = TaskManager.getChannelPackageDtos(channel, i, batchSize);
            packageBatch.elaborate();
            for (PackageDto pkgDto : packageBatch) {
                // this is a sanity check
                // package may have been deleted before packageBatch.elaborate()
                if (pkgDto.getChecksum() == null) {
                    // channel content changed, we cannot guarantee correct repodata
                    throw new RepomdRuntimeException("Package with id " + pkgDto.getId() +
                            " removed from server, interrupting repo generation for " +
                            channel.getLabel());
                }
                primary.addPackage(pkgDto);
                filelists.addPackage(pkgDto);
                other.addPackage(pkgDto);
                susedata.addPackage(pkgDto);
                try {
                    primaryFile.flush();
                    filelistsFile.flush();
                    otherFile.flush();
                    susedataFile.flush();
                }
                catch (IOException e) {
                    throw new RepomdRuntimeException(e);
                }
            }
            log.info("Processed " + (i + packageBatch.getEnd()) + " packages");
        }
        primary.end();
        filelists.end();
        other.end();
        susedata.end();
        try {
            primaryBufferedWriter.close();
            filelistsBufferedWriter.close();
            otherBufferedWriter.close();
            susedataBufferedWriter.close();
        }
        catch (IOException e) {
            throw new RepomdRuntimeException(e);
        }

        RepomdIndexData primaryData = new RepomdIndexData(primaryFile.getCompressedChecksum(),
                primaryFile.getUncompressedChecksum(), channel.getLastModified());
        RepomdIndexData filelistsData = new RepomdIndexData(filelistsFile.getCompressedChecksum(),
                filelistsFile.getUncompressedChecksum(), channel.getLastModified());
        RepomdIndexData otherData = new RepomdIndexData(otherFile.getCompressedChecksum(),
                otherFile.getUncompressedChecksum(), channel.getLastModified());
        RepomdIndexData susedataData = new RepomdIndexData(susedataFile.getCompressedChecksum(),
                susedataFile.getUncompressedChecksum(), channel.getLastModified());

        if (log.isDebugEnabled()) {
            log.debug("Starting updateinfo generation for '" + channel.getLabel() + '"');
        }
        RepomdIndexData updateinfoData = generateUpdateinfo(channel, prefix, checksumAlgo);
        RepomdIndexData productsData = generateProducts(channel, prefix, checksumAlgo);
        RepomdIndexData groupsData = loadRepoMetadataFile(channel, checksumAlgo, GROUP);
        RepomdIndexData modulesData = loadRepoMetadataFile(channel, checksumAlgo, MODULES);

        // Set the type so yum can read and perform checksum
        primaryData.setType(checksumLabel);
        filelistsData.setType(checksumLabel);
        otherData.setType(checksumLabel);
        susedataData.setType(checksumLabel);
        if (updateinfoData != null) {
            updateinfoData.setType(checksumLabel);
        }
        if (groupsData != null) {
            groupsData.setType(checksumLabel);
        }
        if (modulesData != null) {
            modulesData.setType(checksumLabel);
        }
        if (productsData != null) {
            productsData.setType(checksumLabel);
        }

        FileWriter indexFile;
        try {
            indexFile = new FileWriter(prefix + REPOMD_FILE);
            RepomdIndexWriter index = new RepomdIndexWriter(indexFile, primaryData,
                    filelistsData, otherData, susedataData, updateinfoData,
                    groupsData, modulesData, productsData);
            index.writeRepomdIndex();
            indexFile.close();
        }
        catch (IOException e) {
            throw new RepomdRuntimeException(e);
        }

        List<File> createdFiles = new ArrayList<>();
        RepomdFileOrganizer organizer = new RepomdFileOrganizer(prefix, channel.getLastModified().getTime());
        createdFiles.add(organizer.move(PRIMARY_FILE, "primary.xml.gz", primaryData.getChecksum()));
        createdFiles.add(organizer.move(FILELISTS_FILE, "filelists.xml.gz", filelistsData.getChecksum()));
        createdFiles.add(organizer.move(OTHER_FILE, "other.xml.gz", otherData.getChecksum()));
        createdFiles.add(organizer.move(SUSEDATA_FILE, "susedata.xml.gz", susedataData.getChecksum()));

        // Optional files
        if (updateinfoData != null) {
            createdFiles.add(organizer.move(UPDATEINFO_FILE, "updateinfo.xml.gz", updateinfoData.getChecksum()));
        }
        if (productsData != null) {
            createdFiles.add(organizer.move(PRODUCTS_FILE, "products.xml", productsData.getChecksum()));
        }

        String spacewalkMountPt = Config.get().getString(ConfigDefaults.MOUNT_POINT); // To copy over comps files from
        if (groupsData != null) {
            createdFiles.add(organizer.copy(
                    spacewalkMountPt + File.separator + getRepoMetadataRelativeFilename(channel, GROUP),
                    "comps.xml", groupsData.getChecksum()));
        }
        if (modulesData != null) {
            createdFiles.add(organizer.copy(
                    spacewalkMountPt + File.separator + getRepoMetadataRelativeFilename(channel, MODULES),
                    "modules.yaml", modulesData.getChecksum()));
        }

        // Index file should be the last one to be moved; after all the files are ready to be served
        createdFiles.add(organizer.move(REPOMD_FILE, "repomd.xml"));

        if (ConfigDefaults.get().isMetadataSigningEnabled()) {
            String[] signCommand = new String[2];
            signCommand[0] = "mgr-sign-metadata";
            signCommand[1] = prefix + "repomd.xml";
            cmdExecutor.execute(signCommand);
            createdFiles.add(new File(prefix, "repomd.xml.asc"));
            createdFiles.add(new File(prefix, "repomd.xml.key"));
        }
        log.info("Repository metadata generation for '" +
                channel.getLabel() + "' finished in " +
                (int) (new Date().getTime() - start.getTime()) / 1000 + " seconds");

        generateSolv(channel);
        createdFiles.add(organizer.move(SOLV_FILE, "solv"));

        // Clean the directory of obsolete files
        existingFiles.stream()
                .filter(f -> !createdFiles.contains(f))
                .filter(File::exists)
                .forEach(File::delete);
    }

    private void generateSolv(Channel channel) {
        String repodir  = mountPoint + File.separator + pathPrefix +
                          File.separator + channel.getLabel() + File.separator;
        String solvout  = repodir + SOLV_FILE;
        try {
            // Execute the command
            Runtime rt = Runtime.getRuntime();
            String[] command = {REPO2SOLV, "-o", solvout, repodir};
            Process pr = rt.exec(command, new String[]{});

            // Determine the exit value
            int exitVal = pr.waitFor();
            if (exitVal != 0) {
                log.error("Unable to create the solv file for '" +
                          channel.getLabel() + "'");
            }
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Solv file successfully create for '" + channel.getLabel() + "'");
    }

    /**
     * Deletes existing repo and generates file stating that no repo was generated
     * @param channel the channel to do this for
     * @param prefix the directory prefix
     */
    private void generateBadRepo(Channel channel, String prefix) {
        log.warn("No repo will be generated for channel " + channel.getLabel());
        deleteRepomdFiles(channel.getLabel(), false);
        try {
            FileWriter norepo = new FileWriter(prefix + NOREPO_FILE);
            norepo.write("No repo will be generated for channel " +
                    channel.getLabel() + ".\n");
            norepo.close();
        }
        catch (IOException e) {
            log.warn("Cannot create " + NOREPO_FILE + " file.");
        }
    }

    private String getRepoMetadataRelativeFilename(Channel channel, String metadataType) {
        Method method = null;
        try {
            if (metadataType.equals(GROUP)) {
                method = channel.getClass().getMethod("getComps");
            }
            else if (metadataType.equals(MODULES)) {
                method = channel.getClass().getMethod("getModules");
            }
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        if (method == null) {
            return null;
        }

        try {
            RepoMetadata rmd = (RepoMetadata)method.invoke(channel);
            if (rmd != null && rmd.getRelativeFilename() != null) {
                return rmd.getRelativeFilename();
            }
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }

        // Modules file gets copied for cloned channels, so we don't need to recurse to the original channels to get it.
        // (Reverts https://bugzilla.redhat.com/585901 for modules)
        if (channel.isCloned() && metadataType.equals(GROUP)) {
            // use a hack not to use ClonedChannel and it's getOriginal() method
            Long originalChannelId = ChannelManager.lookupOriginalId(channel);
            Channel originalChannel = ChannelFactory.lookupById(originalChannelId);
            return getRepoMetadataRelativeFilename(originalChannel, metadataType);
        }
        return null;
    }

    /**
     *
     * @param channel channel indo
     * @param checksumAlgo checksum algorithm
     * @param metadataType rype of repo metadata file ("groups", "modules")
     * @return repomd index for given channel
     */
    private RepomdIndexData loadRepoMetadataFile(Channel channel, String checksumAlgo, String metadataType) {
        String mount = Config.get().getString(ConfigDefaults.MOUNT_POINT);
        String relativeFilename = getRepoMetadataRelativeFilename(channel, metadataType);

        if (relativeFilename == null) {
            return null;
        }

        File metadataFile = new File(mount + File.separator + relativeFilename);
        FileInputStream stream;
        try {
            stream = new FileInputStream(metadataFile);
        }
        catch (FileNotFoundException e) {
            return null;
        }

        DigestInputStream digestStream;
        try {
            digestStream = new DigestInputStream(stream, MessageDigest
                    .getInstance(checksumAlgo));
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new RepomdRuntimeException(nsae);
        }
        byte[] bytes = new byte[10];

        try {
            while (digestStream.read(bytes) != -1) {
                // no-op
            }
        }
        catch (IOException e) {
            return null;
        }

        Date timeStamp = new Date(metadataFile.lastModified());

        return new RepomdIndexData(StringUtil.getHexString(digestStream
                .getMessageDigest().digest()), null, timeStamp);
    }

    /**
     * Generates update info for given channel
     * @param channel channel info
     * @param prefix repodata file prefix
     * @param checksumtypeIn checksum type
     * @return repodata index
     */
    private RepomdIndexData generateUpdateinfo(Channel channel, String prefix,
            String checksumtypeIn) {

        if (channel.getErrataCount() == 0) {
            return null;
        }

        CompressingDigestOutputWriter updateinfoFile;
        try {
            updateinfoFile = new CompressingDigestOutputWriter(
                    new FileOutputStream(prefix + UPDATEINFO_FILE), checksumtypeIn);
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new RepomdRuntimeException(e);
        }
        BufferedWriter updateinfoBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(updateinfoFile));
        UpdateInfoWriter updateinfo = new UpdateInfoWriter(
                updateinfoBufferedWriter);
        updateinfo.getUpdateInfo(channel);
        try {
            updateinfoBufferedWriter.close();
        }
        catch (IOException e) {
            throw new RepomdRuntimeException(e);
        }

        RepomdIndexData updateinfoData = new RepomdIndexData(updateinfoFile
                .getCompressedChecksum(), updateinfoFile
                .getUncompressedChecksum(), channel.getLastModified());
        return updateinfoData;
    }

    /**
     * Generates product info for given channel
     * @param channel channel info
     * @param checksumtypeIn checksum type
     * @return repodata index
     */
    private RepomdIndexData generateProducts(Channel channel, String prefix, String checksumtypeIn) {

        DigestOutputStream productsFile;
        try {
            productsFile = new DigestOutputStream(
                    new FileOutputStream(prefix + PRODUCTS_FILE),
                    MessageDigest.getInstance(checksumtypeIn));
        }
        catch (FileNotFoundException | NoSuchAlgorithmException e) {
            throw new RepomdRuntimeException(e);
        }
        BufferedWriter productsBufferedWriter = new BufferedWriter(
                new OutputStreamWriter(productsFile));
        SuseProductWriter products = new SuseProductWriter(
                productsBufferedWriter);
        String ret = products.getProducts(channel);
        try {
            productsBufferedWriter.close();
        }
        catch (IOException e) {
            throw new RepomdRuntimeException(e);
        }
        if (ret == null) {
            return null;
        }

        RepomdIndexData productsData = new RepomdIndexData(
                StringUtil.getHexString(productsFile.getMessageDigest().digest()),
                StringUtil.getHexString(productsFile.getMessageDigest().digest()),
                channel.getLastModified());
        return productsData;
    }
}
