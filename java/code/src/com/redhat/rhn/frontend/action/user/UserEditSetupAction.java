/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.user;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UserEditAction, edit action for user detail page
 * @version $Rev: 1196 $
 */
public class UserEditSetupAction extends RhnAction {

    private static Logger log = Logger.getLogger(UserEditSetupAction.class);

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        DynaActionForm form = (DynaActionForm)formIn;

        RequestContext requestContext = new RequestContext(request);

        //UserDetails under /rhn/users needs parameter, but /rhn/account does not
        Long uid = requestContext.getParamAsLong("uid");
        if (request.getRequestURL().toString().indexOf("/rhn/users/") != -1 &&
                uid == null) {
            throw new BadParameterException("Invalid uid for /rhn/users/");
        }
        User loggedInUser = requestContext.getCurrentUser();
        User targetUser = UserManager.lookupUser(requestContext.getCurrentUser(), uid);
        request.setAttribute(RhnHelper.TARGET_USER, targetUser);

        if (targetUser == null) {
            targetUser = loggedInUser;
        }

        form.set("uid", targetUser.getId());
        form.set("firstNames", targetUser.getFirstNames());
        form.set("lastName", targetUser.getLastName());
        form.set("title", targetUser.getTitle());
        form.set("prefix", targetUser.getPrefix());
        request.setAttribute("user", targetUser);
        request.setAttribute("mailableAddress", targetUser.getEmail());

        request.setAttribute("readonly", targetUser.getReadOnlyBool());
        request.setAttribute("created", targetUser.getCreated());
        request.setAttribute("lastLoggedIn", targetUser.getLastLoggedIn());

        setupRoles(request, targetUser);

        // SETUP Prefix list
        request.setAttribute("availablePrefixes", UserActionHelper.getPrefixes());
        request.setAttribute("self", Boolean.valueOf(loggedInUser.equals(targetUser)));

        //Should we display the pam checkbox?
        String pamAuthService = Config.get().getString(
                ConfigDefaults.WEB_PAM_AUTH_SERVICE);
        if (pamAuthService != null && pamAuthService.trim().length() > 0) {
            request.setAttribute("displaypam", "true");
            form.set("usepam", Boolean.valueOf(targetUser.getUsePamAuthentication()));
        }

        // Keep the new list tag happy:
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private void setupRoles(HttpServletRequest request, User targetUser) {
        log.debug("setupRoles()");

        Set<Role> orgRoles = targetUser.getOrg().getRoles();

        List<UserRoleStatusBean> adminRoles = new LinkedList<UserRoleStatusBean>();
        List<UserRoleStatusBean> regularRoles = new LinkedList<UserRoleStatusBean>();

        // Bit of a hack here. We're trying to represent three states to the processing
        // code with a checkbox that can only submit two. (i.e., there's no way to
        // differentiate between a checkbox that was de-selected and one that was
        // disabled when submitting the form.
        //
        // We ran this many different ways but nothing was as intuitive to the user as
        // the simple checkbox interface. Thus we hack around the problem by storing a list
        // of the disabled roles, bar separated. This allows us to add the extra info we
        // need when processing the form.
        StringBuilder disabledRoles = new StringBuilder();

        for (Role currRole : orgRoles) {
            log.debug("currRole = " + currRole.getLabel());

            boolean selected = false; // does user have this role?
            boolean disabled = false; // is the role modifiable?

            String uilabel =
                LocalizationService.getInstance().getMessage(currRole.getLabel());
            String uivalue = currRole.getLabel();

            if (targetUser.hasPermanentRole(currRole)) {
                selected = true;
                log.debug("1");
            }

            // If the Role is a member of the implied roles
            // then tack on an extra string at the end
            // and disable the item in the UI.
            if (UserFactory.IMPLIEDROLES.contains(currRole) &&
                    targetUser.hasPermanentRole(RoleFactory.ORG_ADMIN)) {
                StringBuilder sb = new StringBuilder();
                sb.append(uilabel);
                sb.append(" - [ ");
                sb.append(LocalizationService.getInstance().getMessage("Admin Access"));
                sb.append(" ]");
                uilabel = sb.toString();

                disabled = true;
                log.debug("2");
            }

            //sat admin can not be modified outside sat tools
            if (currRole.equals(RoleFactory.SAT_ADMIN)) {
                disabled = true;
                log.debug("4");
            }

            log.debug("   selected = " + selected);
            log.debug("   disabled = " + disabled);
            if (currRole.equals(RoleFactory.SAT_ADMIN) ||
                    currRole.equals(RoleFactory.ORG_ADMIN)) {
                adminRoles.add(new UserRoleStatusBean(uilabel, uivalue, selected,
                        disabled));
            }
            else {
                regularRoles.add(new UserRoleStatusBean(uilabel, uivalue, selected,
                        disabled));
            }

            if (disabled) {
                if (disabledRoles.length() > 0) {
                    disabledRoles.append("|");
                }
                disabledRoles.append(currRole.getLabel());
            }
        }

        boolean hasOrgAdmin = false;
        if (targetUser.hasPermanentRole(RoleFactory.ORG_ADMIN)) {
            hasOrgAdmin = true;
        }

        request.setAttribute("adminRoles", adminRoles);
        request.setAttribute("regularRoles", regularRoles);
        request.setAttribute("disabledRoles", disabledRoles);
        Set<Role> tempRoles = targetUser.getTemporaryRoles();
        if (!tempRoles.isEmpty()) {
            request.setAttribute("temporaryRoles", UserManager.roleNames(tempRoles));
        }

        request.setAttribute("orgAdmin", hasOrgAdmin);
    }
}
