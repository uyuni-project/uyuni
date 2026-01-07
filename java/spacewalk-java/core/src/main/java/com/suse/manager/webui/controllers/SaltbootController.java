/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class for image file upload.
 */
public class SaltbootController {

    private static final Logger LOG = LogManager.getLogger(SaltbootController.class);

    private SaltbootController() { }

    /**
     * Initialize request routes for the pages served by SaltbootController
     *
     */
    public static void initRoutes() {

        Spark.get("/saltboot/*", SaltbootController::redirectImage);
    }


    private static Optional<String> mapPillarUrl(Map<String, Object> pillar, String path) {
        try {
            Map<String, Object> images = (Map<String, Object>)pillar.get("images");
            Map<String, Object> image = (Map<String, Object>)images.entrySet().iterator().next().getValue();
            Map<String, Object> imageVer = (Map<String, Object>)image.entrySet().iterator().next().getValue();
            String imageUrl = (String)imageVer.get("url");
            URL parsed = new URL(imageUrl);
            LOG.debug("Have image {}", parsed.getPath());
            if (parsed.getPath().equals(path)) {
                Map<String, Object> sync = (Map<String, Object>)imageVer.get("sync");
                String mapped = (String)sync.get("url");
                return Optional.ofNullable(mapped);
            }
        }
        catch (NullPointerException e) {
            LOG.error("Invalid image pillar", e);
        }
        catch (MalformedURLException e) {
            LOG.error("Malformed image url", e);
        }

        try {
            Map<String, Object> images = (Map<String, Object>)pillar.get("boot_images");
            Map<String, Object> image = (Map<String, Object>)images.entrySet().iterator().next().getValue();
            Map<String, Object> initrd = (Map<String, Object>)image.get("initrd");
            Map<String, Object> sync = (Map<String, Object>)image.get("sync");
            String initrdUrl = (String)initrd.get("url");
            URL initrdParsed = new URL(initrdUrl);
            LOG.debug("Have initrd {}", initrdParsed.getPath());
            if (initrdParsed.getPath().equals(path)) {
                String mapped = (String)sync.get("initrd_url");
                return Optional.ofNullable(mapped);
            }
            Map<String, Object> kernel = (Map<String, Object>)image.get("kernel");
            String kernelUrl = (String)kernel.get("url");
            URL kernelParsed = new URL(kernelUrl);
            LOG.debug("Have kernel {}", kernelParsed.getPath());
            if (kernelParsed.getPath().equals(path)) {
                String mapped = (String)sync.get("kernel_url");
                return Optional.ofNullable(mapped);
            }
        }
        catch (NullPointerException e) {
            LOG.error("Invalid boot image pillar", e);
        }
        catch (MalformedURLException e) {
            LOG.error("Malformed boot image url", e);
        }
        return Optional.empty();
    }

    /**
     * Redirect image url
     * this provides the image syncing info to containerized proxy, which
     * can't access image pillars via salt.
     *
     * @param request the request
     * @param response the response
     * @return empty string
     */
    public static String redirectImage(Request request, Response response) {
        Long orgId = 1L;
        try {
            orgId = Long.parseLong(request.queryParams("orgid"));
        }
        catch (NumberFormatException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        Org org = OrgFactory.lookupById(orgId);

        Optional<String> mapped = ImageInfoFactory.listImageInfos(org).stream()
            .map(image -> image.getPillar())
            .filter(Objects::nonNull)
            .map(pillar -> mapPillarUrl(pillar.getPillar(), request.pathInfo()))
            .flatMap(Optional::stream)
            .findFirst();

        String logPath = request.pathInfo().replaceAll("[\n\r]", "_");
        if (mapped.isPresent()) {
            LOG.info("Redirecting {} to {}", logPath, mapped.get());
            response.redirect(mapped.get(), HttpStatus.SC_MOVED_PERMANENTLY);
        }
        else {
            LOG.error("Image not found in pillars: {}", logPath);
            Spark.halt(HttpStatus.SC_NOT_FOUND, "Image not found in pillars");
        }
        return "";
    }
}
