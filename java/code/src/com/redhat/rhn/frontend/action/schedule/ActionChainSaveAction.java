/*
 * Copyright (c) 2014 SUSE LLC
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
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */
package com.redhat.rhn.frontend.action.schedule;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stringtree.json.JSONWriter;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles Ajax requests for the Action Chain Edit page.
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class ActionChainSaveAction {

    /** JSON result object field name. */
    public static final String SUCCESS_FIELD = "success";

    /** JSON result object field name. */
    public static final String TEXT_FIELD = "text";

    /** Logger instance. */
    private static Logger log = LogManager.getLogger(ActionChainSaveAction.class);

    /**
     * Saves changes to an Action Chain.
     * @param actionChainId the action chain id
     * @param label the new label
     * @param deletedEntries list of deleted entries id
     * @param deletedSortOrders list of sort order values to delete
     * @param reorderedSortOrders non-deleted sort order numbers in new order
     * @param request HTTP request object
     * @return a JSON object with a success field and a text field
     */
    public String save(Long actionChainId, String label, List<Long> deletedEntries,
            List<Integer> deletedSortOrders, List<Integer> reorderedSortOrders,
            HttpServletRequest request) {
        try {
            User u = new RequestContext(request).getCurrentUser();
            ActionChain actionChain = ActionChainFactory.getActionChain(u, actionChainId);

            // input validation

            if (label.length() >= 256) {
                log.debug("Action Chain label {} is too long (>= 256 chars)", label);
                return makeResult(false, "actionchain.jsp.labeltoolong");
            }
            ActionChain sameLabelActionChain = ActionChainFactory.getActionChain(u, label);
            if (sameLabelActionChain != null &&
                !sameLabelActionChain.getId().equals(actionChainId)) {
                log.debug("Action Chain label {} exists", label);
                return makeResult(false, "actionchain.jsp.labelexists");
            }

            // change label
            log.debug("Editing Action Chain {}, changing label to {}", actionChain, label);
            actionChain.setLabel(ActionChainHelper.sanitizeLabel(label));

            // delete entries
            for (Long id : deletedEntries) {
                log.debug("Deleting entry {}", id);
                actionChain.getEntries().remove(
                    ActionChainFactory.getActionChainEntry(u, id));
            }

            // delete groups
            for (Integer sortOrder : deletedSortOrders) {
                log.debug("Deleting group with sort order {}", sortOrder);
                List<ActionChainEntry> entries = ActionChainFactory.getActionChainEntries(
                    actionChain, sortOrder);
                actionChain.getEntries().removeAll(entries);
            }

            // update groups' sort order
            List<List<ActionChainEntry>> entryGroups =
                    new LinkedList<>();
            for (Integer reorderedSortOrderIn : reorderedSortOrders) {
                entryGroups.add(ActionChainFactory.getActionChainEntries(
                        actionChain, reorderedSortOrderIn));
            }
            for (int sortOrder = 0; sortOrder < reorderedSortOrders.size(); sortOrder++) {
                log.debug("Changing group order from {} to {}", entryGroups.get(sortOrder), sortOrder);
                for (ActionChainEntry entry : entryGroups.get(sortOrder)) {
                    entry.setSortOrder(sortOrder);
                }
            }

            // update modification date
            actionChain.setModified(new Date());

            return makeResult(true, "actionchain.jsp.saved");
        }
        catch (Exception e) {
            log.error("Unexpected exception while processing AJAX call", e);
            throw e;
        }
    }

    /**
     * Returns a result object as a string.
     * @param success true if the save was successful
     * @param messageId the message id for the text
     * @return a JSON string
     */
    private String makeResult(boolean success, String messageId) {
        Map<String, Object> result = new HashMap<>();
        result.put(SUCCESS_FIELD, success);
        result.put(TEXT_FIELD, LocalizationService.getInstance().getMessage(messageId));
        return new JSONWriter().write(result);
    }
}
