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
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.BaseDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.list.ColumnFilter;
import com.redhat.rhn.frontend.taglibs.list.ListFilter;
import com.redhat.rhn.frontend.taglibs.list.ListFilterHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagUtil;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.common.util.DynamicComparator;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DownloadAction;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expects that the following parameters are available from the request object:
 * EXPORT_COLUMNS set to the value of the session attribute containing the
 * exportColumns
 * PAGE_LIST_DATA set to the value of the session attribute containg the List
 * of items to export
 * UNIQUE_NAME set to the value of the uniqueName associated with this list
 * from the CSVTag
 *
 * @author jmatthews
 */
public class CSVDownloadAction extends DownloadAction {

    private static final Logger LOG = LogManager.getLogger(CSVDownloadAction.class);

    public static final String EXPORT_COLUMNS = "__CSV__exportColumnsParam";
    public static final String PAGE_LIST_DATA = "___CSV_pageListData";
    public static final String QUERY_DATA = "__CSV_queryMode";
    public static final String HEADER_NAME = "__CSV_headerName";

    private static final Map<String, String> FILTER_ALIASES = Map.ofEntries(
            Map.entry("column", "com.redhat.rhn.frontend.taglibs.list.ColumnFilter"),
            Map.entry("configChannel", "com.redhat.rhn.frontend.action.configuration.ConfigChannelFilter"),
            Map.entry("viewModifyPaths", "com.redhat.rhn.frontend.action.configuration.sdc.ViewModifyPathsFilter"),
            Map.entry("org", "com.redhat.rhn.frontend.action.multiorg.OrgListFilter"),
            Map.entry("trust", "com.redhat.rhn.frontend.action.multiorg.TrustListFilter"),
            Map.entry("user", "com.redhat.rhn.frontend.action.multiorg.UserListFilter"),
            Map.entry("ksIpRange", "com.redhat.rhn.frontend.action.kickstart.KickstartIpRangeFilter"),
            Map.entry("ksProfile", "com.redhat.rhn.frontend.action.kickstart.KickstartProfileFilter"),
            Map.entry("packageName", "com.redhat.rhn.frontend.action.channel.PackageNameFilter"),
            Map.entry("packageNVREA", "com.redhat.rhn.frontend.action.channel.PackageNVREAFilter"),
            Map.entry("appStream", "com.redhat.rhn.frontend.action.channel.AppStreamFilter"),
            Map.entry("errata", "com.redhat.rhn.frontend.action.channel.manage.ErrataFilter")
    );

    private static final Map<String, String> REVERSE_FILTER_ALIASES = FILTER_ALIASES.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Returns the alias for a given filter class name, or the class name itself if not found.
     * @param className filter class name
     * @return alias or class name
     */
    public static String getFilterAlias(String className) {
        return REVERSE_FILTER_ALIASES.getOrDefault(className, className);
    }

    /**
     * Returns the filter class name for a given alias.
     * @param alias alias
     * @return class name or null if not found
     */
    public static String getFilterClassName(String alias) {
        return FILTER_ALIASES.get(alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.execute(mapping, form, request, response);
        } catch (Exception e) {
            /*
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
    protected List<?> getPageData(HttpServletRequest request, HttpSession session) {
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
            throw new IllegalArgumentException("Missing request parameter, " + PAGE_LIST_DATA);
        }

        List<?> pageData = (List<?>) session.getAttribute(paramPageData);
        if (pageData == null) {
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
        String uniqueName = getUniqueName(request);

        // Read the CSV separator from user preferences
        // FIX: RequestContext can be null in tests
        User user = null;
        try {
            user = new RequestContext(request).getCurrentUser();
        } catch (Exception e) {
            // Ignore, common in test environment
        }

        // FIX: Fallback to comma if user or preferences are missing
        char separator = (user != null) ? user.getCsvSeparator() : ',';

        // Re-apply list filtering if parameters are present
        pageData = applyListFilterForCsv(pageData, request);

        CSVWriter csvWriter = new CSVWriter(new StringWriter(), separator);
        csvWriter.setColumns(Arrays.stream(exportColumns.split(",")).map(String::trim).collect(Collectors.toList()));

        String header = getHeaderText(request, session);
        if (header != null) {
            csvWriter.setHeaderText(header);
        }

        Elaborator elaborator = TagHelper.lookupElaboratorFor(getUniqueName(request), request);
        if (elaborator != null) {
            elaborator.elaborate(new ArrayList<>(pageData), HibernateFactory.getSession());
        }

        String contentType = csvWriter.getMimeType() + ";charset=" + response.getCharacterEncoding();
        response.setHeader("Content-Disposition", "attachment; filename=download." + csvWriter.getFileExtension());
        csvWriter.write(pageData);

        return new ByteArrayStreamInfo(contentType, csvWriter.getContents().getBytes());
    }

    /**
     * When CSV export re-runs a stored
     * {@link com.redhat.rhn.common.db.datasource.CachedStatement}
     * query, the result is unfiltered. Re-apply the same list filter the UI used,
     * using parameters
     * appended to the download link by
     * {@link com.redhat.rhn.frontend.taglibs.list.CSVTag}.
     */
    protected List<BaseDto> applyListFilterForCsv(List<BaseDto> pageData, HttpServletRequest request) {
        if (pageData == null || request == null) {
            return pageData;
        }
        String name = getUniqueName(request);
        if (StringUtils.isBlank(name)) {
            return pageData;
        }
        String val = ListTagHelper.getFilterValue(request, name);
        String by = request.getParameter(ListTagUtil.makeFilterByLabel(name));
        if (StringUtils.isBlank(val) || StringUtils.isBlank(by)) {
            return pageData;
        }
        String attr = request.getParameter(ListTagUtil.makeFilterAttributeByLabel(name));
        if (StringUtils.isNotBlank(attr)) {
            return filterDtosByPropertyContains(pageData, attr, val);
        }
        String classNameOrAlias = request.getParameter(ListTagUtil.makeFilterClassLabel(name));
        if (StringUtils.isBlank(classNameOrAlias) || ColumnFilter.class.getCanonicalName().equals(classNameOrAlias)) {
            return pageData;
        }

        // Security check: resolve alias and check whitelist
        String className = FILTER_ALIASES.get(classNameOrAlias);
        if (className == null) {
            // Check if it's a full class name that is in our whitelist values (for legacy support if needed)
            if (FILTER_ALIASES.containsValue(classNameOrAlias)) {
                className = classNameOrAlias;
            } else {
                throw new SecurityException("Unauthorized filter class requested: " + classNameOrAlias);
            }
        }

        try {
            Class<?> klass;
            try {
                klass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                klass = Thread.currentThread().getContextClassLoader().loadClass(className);
            }
            ListFilter filter = (ListFilter) klass.getDeclaredConstructor().newInstance();
            filter.prepare(resolveLocale());
            return ListUtils.typedList(ListFilterHelper.filter(pageData, filter, by, val), BaseDto.class);
        } catch (Exception e) {
            throw new RhnRuntimeException("CSV export: filter error", e);
        }
    }

    private static List<BaseDto> filterDtosByPropertyContains(List<BaseDto> data, String attr, String criteria) {
        String c = criteria.toLowerCase();
        return data.stream().filter(dto -> {
            try {
                String val = ListTagUtil.getBeanValue(dto, attr);
                return val != null && val.toLowerCase().contains(c);
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private static Locale resolveLocale() {
        Context ctx = Context.getCurrentContext();
        return (ctx != null) ? ctx.getLocale() : Locale.getDefault();
    }
}
