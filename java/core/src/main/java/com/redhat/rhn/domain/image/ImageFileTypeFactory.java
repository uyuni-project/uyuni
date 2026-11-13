/*
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Factory for {@link ImageFileType}
 */
public class ImageFileTypeFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(ImageFileTypeFactory.class);

    private static ImageFileTypeFactory instance = new ImageFileTypeFactory();

    private ImageFileTypeFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Return the file types enabled for an organization, keyed by their extension. An entry of the
     * organization overrides the default entry with the same extension.
     *
     * @param org the organization owning the build
     * @return the enabled file types by extension
     */
    public static Map<String, ImageFileType> lookupEnabledFileTypes(Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ImageFileType> criteria = builder.createQuery(ImageFileType.class);
        Root<ImageFileType> root = criteria.from(ImageFileType.class);
        criteria.where(builder.or(builder.isNull(root.get("org")), builder.equal(root.get("org"), org)));

        Map<String, ImageFileType> typesByExtension = new HashMap<>();
        List<ImageFileType> types = getSession().createQuery(criteria).getResultList();
        // defaults first, so the entries of the organization replace them
        types.stream().filter(type -> type.getOrg() == null)
                .forEach(type -> typesByExtension.put(type.getExtension(), type));
        types.stream().filter(type -> type.getOrg() != null)
                .forEach(type -> typesByExtension.put(type.getExtension(), type));
        typesByExtension.values().removeIf(type -> !type.isEnabled());
        return typesByExtension;
    }

    /**
     * Keep only the files which are the image itself (file named exactly as the basename, which is an
     * uncompressed PXE/KIS image) or which have an extension enabled for the organization, mapped to the
     * filetype of the matched extension. When several extensions match, the longest one wins.
     *
     * @param paths file paths or file names
     * @param basename the basename of the image
     * @param org the organization owning the build
     * @return the allowed subset of the given paths mapped to their filetype, in the original order
     */
    public static Map<String, String> filterAllowedFiles(List<String> paths, String basename, Org org) {
        Map<String, ImageFileType> typesByExtension = lookupEnabledFileTypes(org);
        Map<String, String> allowedFiles = new LinkedHashMap<>();
        for (String path : paths) {
            String fileName = Path.of(path).getFileName().toString();
            if (fileName.equals(basename)) {
                allowedFiles.put(path, "image");
            }
            else {
                typesByExtension.entrySet().stream()
                        .filter(entry -> fileName.endsWith(entry.getKey()))
                        .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                        .ifPresent(entry -> allowedFiles.put(path, entry.getValue().getFiletype()));
            }
        }
        return allowedFiles;
    }
}
