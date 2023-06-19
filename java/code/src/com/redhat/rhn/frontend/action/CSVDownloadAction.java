/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.db.datasource.CachedStatement;
import com.redhat.rhn.common.db.datasource.Elaborator;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.CSVWriter;
import com.redhat.rhn.common.util.download.ByteArrayStreamInfo;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.BaseDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DownloadAction;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Expects that the following parameters are available from the request object:
 *  EXPORT_COLUMNS set to the value of the session attribute containing the
 *      exportColumns
 *  PAGE_LIST_DATA set to the value of the session attribute containg the List
 *      of items to export
 *  UNIQUE_NAME set to the value of the uniqueName associated with this list
 *      from the CSVTag
 *
 * @author jmatthews
 */
public class CSVDownloadAction extends DownloadAction {

    private static final Logger LOG = LogManager.getLogger(CSVDownloadAction.class);

    public static final String EXPORT_COLUMNS = "__CSV__exportColumnsParam";
    public static final String PAGE_LIST_DATA = "___CSV_pageListData";
    public static final String QUERY_DATA = "__CSV_queryMode";
    public static final String UNIQUE_NAME = "__CSV_uniqueName";
    public static final String HEADER_NAME = "__CSV_headerName";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
        try {
            super.execute(mapping, form, request, response);
        }
        catch (Exception e) {
            /**
             * Overridden to redirect for case of errors while processing CSV Export,
             * example: Session timeout.
             */
            LOG.error("Failed to generate CSV", e);
            return mapping.findForward("error");
        }
        return null;
    }

    /**
     * Returns String containing a comma separated list of names to represent the
     * header values of the List or throws Exception if request attribute
     * EXPORT_COLUMNN is missing or session attribute is null.
     *
     * @param request HTTP request
     * @param session HTTP session
     * @return exported columns
     */
    protected String getExportColumns(HttpServletRequest request, HttpSession session) {
        String paramExportColumns = request.getParameter(EXPORT_COLUMNS);
        if (null == paramExportColumns) {
            throw new IllegalArgumentException("Missing request parameter, " + EXPORT_COLUMNS);
        }
        String exportColumns = (String) session.getAttribute(paramExportColumns);
        if (null == exportColumns) {
            throw new IllegalArgumentException("Missing value for session attribute, " +
                    paramExportColumns);
        }
        return exportColumns;
    }

    /**
     * Returns List of data referred to by session attribute with the name
     * PAGE_LIST_DATA. Throws Exception if request attribute PAGE_LIST_DATA is
     * missing or session attribute is null.
     *
     * @param request HTTP Request
     * @param session HTTP session
     * @return page data
     */
    @SuppressWarnings("unchecked")
    protected List<BaseDto> getPageData(HttpServletRequest request, HttpSession session) {
        String paramQuery = request.getParameter(QUERY_DATA);
        if (paramQuery != null) {
            CachedStatement query = (CachedStatement) session.getAttribute(paramQuery);
            if (query == null) {
                throw new IllegalArgumentException("Missing request parameter, " + QUERY_DATA);
            }
            return query.restartQuery(HibernateFactory.getSession());
        }

        String paramPageData = request.getParameter(PAGE_LIST_DATA);
        if (null == paramPageData) {
            throw new IllegalArgumentException("Missing request parameter, " + EXPORT_COLUMNS);
        }

        List<BaseDto> pageData = (List<BaseDto>) session.getAttribute(paramPageData);

        // You would think that since the line above didn't throw an exceptions, then the list items are guaranteed
        // to be of type BaseDto, but no.
        // The casting above will succeed as long as getAttribute returns a List; it doesn't matter what the generic is.
        if (pageData != null && !pageData.isEmpty() && !(pageData.get(0) instanceof BaseDto)) {
            throw new IllegalStateException("Data to be exported as CSV should inherit BaseDto");
        }

        if (null == pageData) {
            throw new IllegalArgumentException("Missing value for session attribute, " + paramPageData);
        }
        return pageData;
    }

    /**
     * Returns the value of the UNIQUE_NAME attribute or exception if value is null.
     *
     * @param request HTTP request containing UNIQUE_NAME parameter
     * @return unique name
     */
    protected String getUniqueName(HttpServletRequest request) {
        String uniqueName = request.getParameter(UNIQUE_NAME);
        if (uniqueName == null) {
            throw new IllegalArgumentException("Missing request parameter, " + UNIQUE_NAME);
        }
        return uniqueName;
    }

    /**
     * Returns the header name
     *
     * @param request the http servlet request
     * @param session the session
     * @return the header name
     */
    protected String getHeaderText(HttpServletRequest request, HttpSession session) {
        String paramHeader = request.getParameter(HEADER_NAME);
        if (null == paramHeader) {
            // this is an optional parameter, return null if it's not there.
            return null;

        }
        String header = (String) session.getAttribute(paramHeader);
        if (null == header) {
            throw new IllegalArgumentException("Missing value for session attribute, " + paramHeader);
        }
        return header;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected StreamInfo getStreamInfo(ActionMapping mapping, ActionForm form,
                                       HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (null == session) {
            throw new RhnRuntimeException("Missing session");
        }

        String exportColumns = getExportColumns(request, session);
        List<BaseDto> pageData = getPageData(request, session);

        // Read the CSV separator from user preferences
        User user = new RequestContext(request).getCurrentUser();
        CSVWriter csvWriter = new CSVWriter(new StringWriter(), user.getCsvSeparator());
        String[] columns = exportColumns.split("\\s*,\\s*");
        csvWriter.setColumns(Arrays.asList(columns));

        String header = getHeaderText(request, session);
        if (header != null) {
            csvWriter.setHeaderText(header);
        }

        Elaborator elaborator = TagHelper.lookupElaboratorFor(getUniqueName(request), request);
        if (elaborator != null) {
            elaborator.elaborate(pageData, HibernateFactory.getSession());
        }

        String contentType = csvWriter.getMimeType() + ";charset=" + response.getCharacterEncoding();
        response.setHeader("Content-Disposition", "attachment; filename=download." + csvWriter.getFileExtension());
        csvWriter.write(pageData);

        return new ByteArrayStreamInfo(contentType, csvWriter.getContents().getBytes());
    }
}
