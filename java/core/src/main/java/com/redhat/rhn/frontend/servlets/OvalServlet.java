/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.util.OvalFileAggregator;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataFile;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.errata.ErrataManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet interface for downloading OVAL files
 *
 */
public class OvalServlet extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(OvalServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        User user = new RequestContext(request).getCurrentUser();
        List<String> errataIds = getErrataIds(request, response);
        if (errataIds.isEmpty()) {
            return;
        }
        String format = getFormat(request);

        List<Errata> erratas = errataIds.stream()
                .flatMap(id -> ErrataManager.lookupErrataByIdentifier(id, user.getOrg()).stream())
                .toList();
        if (erratas.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, errataIds.get(0));
            return;
        }
        List<ErrataFile> ovalFiles = new LinkedList<>();
        if (erratas.size() == 1) {
            Errata errata = erratas.get(0);

            List<ErrataFile> of =
                ErrataFactory.lookupErrataFilesByErrataAndFileType(errata.getId(), "oval");
            if (of != null && !of.isEmpty()) {
                ovalFiles = of;
            }
        }
        else {
            ovalFiles = erratas.stream()
                    .flatMap(errata -> ErrataFactory.lookupErrataFilesByErrataAndFileType(errata.getId(), "oval")
                            .stream())
                    .collect(Collectors.toList());
        }

        if (format.equals("xml")) {
            streamXml(ovalFiles, response);
        }
        else {
            prepareZipFile(ovalFiles, response);
        }
    }

    private List<String> getErrataIds(HttpServletRequest request, HttpServletResponse response) {
        String[] errataIds = request.getParameterValues("errata");
        if (errataIds == null || errataIds.length == 0) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return Collections.emptyList();
        }
        return Arrays.stream(errataIds).map(id -> URLDecoder.decode(id, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }

    private String getFormat(HttpServletRequest request) {
        String format = request.getParameter("format");
        if (format == null || (!format.equalsIgnoreCase("xml") &&
                !format.equals("zip"))) {
            format = "xml";
        }
        else {
            format = format.toLowerCase();
        }
        return format;
    }

    private void prepareZipFile(List<ErrataFile> ovalFiles, HttpServletResponse response) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("rhn", "errata");
        }
        catch (IOException e) {
            LOG.error("Failed to create temporary file", e);
            return;
        }

        List<File> files = ErrataManager.resolveOvalFiles(ovalFiles);
        if (files.isEmpty()) {
            return;
        }
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempFile))) {
            for (File f : files) {
                ZipEntry entry = new ZipEntry(f.getName());
                zipOut.putNextEntry(entry);
                writeFileEntry(f, zipOut);
            }
            zipOut.flush();
            streamZipFile(tempFile, response);
        }
        catch (IOException e) {
            LOG.error("Failed to create Zip file", e);
        }
        finally {
            try {
                Files.delete(tempFile.toPath());
            }
            catch (IOException e1) {
                LOG.error("Failed to delete temporary file", e1);
            }
        }
    }

    private void streamZipFile(File zipFile, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.addHeader("Content-disposition", "attachment; filename=oval.zip");
        if (zipFile.length() < Integer.MAX_VALUE) {
            response.setContentLength((int) zipFile.length());
        }
        try (InputStream fileIn = new FileInputStream(zipFile)) {
            sendFileContents(fileIn, response);
        }
    }

    private void sendFileContents(InputStream contents,
            HttpServletResponse response) throws IOException {
        try (contents) {
            OutputStream out = response.getOutputStream();
            byte[] chunk = new byte[4096];
            int readsize = -1;
            while ((readsize = contents.read(chunk)) > -1) {
                out.write(chunk, 0, readsize);
            }
        }
    }

    private void writeFileEntry(File f, ZipOutputStream zipOut) throws IOException {
        byte[] chunk = new byte[4096];
        int readsize = -1;
        try (InputStream fileIn = new FileInputStream(f)) {
            while ((readsize = fileIn.read(chunk)) > -1) {
                zipOut.write(chunk, 0, readsize);
            }
            zipOut.closeEntry();
        }
    }

    private void streamXml(List<ErrataFile> files, HttpServletResponse response) {
        response.setContentType("text/xml");
        String fileName = null;
        List<File> ovalFiles = ErrataManager.resolveOvalFiles(files);
        switch(ovalFiles.size()) {
            case 0:
                return;
            case 1:
                File ftmp = ovalFiles.get(0);
                if (ftmp == null) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, files.get(0).toString());
                    return;
                }
                fileName = ftmp.getName().toLowerCase();
                if (!fileName.endsWith(".xml")) {
                    fileName += ".xml";
                }
                break;
            default:
                fileName = "oval.xml";
                break;
        }
        response.addHeader("Content-disposition", "attachment; filename=" + fileName);
        if (ovalFiles.size() == 1) {
            File f = ovalFiles.get(0);
            if (f.length() < Integer.MAX_VALUE) {
                response.setContentLength((int) f.length());
            }
            try (InputStream fileIn = new FileInputStream(f)) {
                sendFileContents(fileIn, response);
            }
            catch (IOException e) {
                LOG.error("Failed to send file contents", e);
            }
        }
        else {
            try {
                String aggregate = aggregateOvalFiles(ovalFiles);
                response.getWriter().print(aggregate);
                response.getWriter().flush();
            }
            catch (Exception e) {
                LOG.error(e.getMessage(), e);
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void sendError(HttpServletResponse response, int code) {
        try {
            response.sendError(code);
        }
        catch (IOException e) {
            LOG.error("Failed to send error response", e);
        }
    }

    private void sendError(HttpServletResponse response, int code, String data) {
        try {
            response.sendError(code, data);
        }
        catch (IOException e) {
            LOG.error("Failed to send error response", e);
        }
    }

    private String aggregateOvalFiles(List<File> files)
            throws JDOMException, IOException {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        String retval = null;
        for (File f : files) {
            if (f == null) {
                continue;
            }
            aggregator.add(f);
        }
        retval = aggregator.finish(false);

        return retval;
    }
}
