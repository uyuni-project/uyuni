/*
 * Copyright (c) 2015 Red Hat, Inc.
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.UserDefaults;
import com.redhat.rhn.common.db.ResetPasswordFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.UserPasswordUtils;
import com.redhat.rhn.domain.common.ResetPassword;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.utils.MailHelper;
import com.suse.manager.webui.utils.LoginHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResetPasswordSubmitAction, responds to user pushing 'update' on the change-password
 * form
 *
 */
public class ResetPasswordSubmitAction extends UserEditActionHelper {

    private static Logger log = LogManager.getLogger(ResetPasswordSubmitAction.class);

    private static final String SUCCESS = "success";
    private static final String MISMATCH = "mismatch";
    private static final String BADPWD = "badpwd";
    private static final String INVALID = "invalid";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
                    HttpServletRequest request, HttpServletResponse response) {

        log.debug("ResetPasswordSubmitAction");
        DynaActionForm form = (DynaActionForm) formIn;
        Map<String, Object> params = makeParamMap(request);

        String token = (form.get("token") == null ? null : form.get("token").toString());
        ResetPassword rp = ResetPasswordFactory.lookupByToken(token);
        ActionErrors errors = ResetPasswordFactory.findErrors(rp);

        // If there are any token-failures - reject and leave
        if (!errors.isEmpty()) {
            log.debug("passwdchange: invalid token!");
            addErrors(request, errors);
            return getStrutsDelegate().forwardParams(mapping.findForward(INVALID), params);
        }

        // We have a valid token - log in the associated user, send them along
        User u = UserFactory.lookupById(rp.getUserId());
        if (u.isDisabled()) {
            log.debug("passwdchange: disabled user found");
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                     new ActionMessage("resetpassword.jsp.error.disabled_user"));
            addErrors(request, errors);
            return getStrutsDelegate().forwardParams(mapping.findForward(INVALID), params);
        }

        // Add an error in case of password mismatch and ignore remaining pwd rules -
        // if the pwds don't match, assume the user finger-fumbled, no sense in yelling
        // at them more
        String pw = (String) form.get("password");
        String conf = (String) form.get("passwordConfirm");
        if (!pw.equals(conf)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                       "error.password_mismatch"));
            addErrors(request, errors);
            return getStrutsDelegate().forwardParams(mapping.findForward(MISMATCH), params);
        }
        
        Map<String, String> errorMap = new HashMap<>();
        // Validate the rest of the password rules
        UserPasswordUtils.validatePassword(errorMap, pw);
        errorMap.forEach((i,k) -> errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(i, k)));
        if (!errors.isEmpty()) {
            addErrors(request, errors);
            return getStrutsDelegate().forwardParams(mapping.findForward(BADPWD), params);
        }

        // If we got this far, we can change the user - update pw and data
        updateUser(u, pw);

        // Send confirmation email
        String emailBody = MailHelper.composeEmailBody("email.reset.password",
                                          u.getEmail(), u.getLogin(),
                                          ConfigDefaults.get().getHostname());
        String subject = MailHelper.PRODUCT_PREFIX + LocalizationService.getInstance().
                getMessage("help.credentials.jsp.passwordreset.confirmation");
        String rhnHeader = "Requested " + subject + " for " + u.getEmail();
        MailHelper.withSmtp().addRhnHeader(rhnHeader).sendEmail(u.getEmail(), subject, emailBody);

        // invalidate any other tokens for them
        ResetPasswordFactory.invalidateUserTokens(u.getId());

        // Set up user to be logged in and sent to YourRhn
        loginAndRedirect(u, mapping, request, response);

        log.debug("ResetLinkAction: user [{}] is now logged in", u.getId());

        // Have to return NULL - updateWebUserId() has already redirected us,
        // and doing it again will make struts Very Angry
        return null;
    }

    private void loginAndRedirect(User u, ActionMapping mapping,
                    HttpServletRequest request, HttpServletResponse response) {
        // Store a "we did it" message
        ActionMessages msgs = new ActionMessages();
        msgs.add(ActionMessages.GLOBAL_MESSAGE,
                 new ActionMessage("message.userInfoUpdated"));
        getStrutsDelegate().saveMessages(request, msgs);

        // update session with actual user
        PxtSessionDelegateFactory.getInstance().newPxtSessionDelegate().
            updateWebUserId(request, response, u.getId());

        // NOTE: following code taken from LoginHelper.successfulLogin().
        // Because that method relies on url_redirect already being set in
        // request, we can't just call it, alas. We have to set url_redirect
        // in order to go where we want, because the updateWebUserId() call
        // resets the web-session and we can't fwd anywhere after that.
        // Fun!
        // Set up to redirect to the 'success' forward in the struts-cfg
        // (probably YourRhn)
        String urlBounce = "/rhn" + mapping.findForward(SUCCESS).getPath();
        String reqMethod = "GET";
        urlBounce = LoginHelper.updateUrlBounce(urlBounce, reqMethod);
        try {
            if (urlBounce != null) {
                log.info("redirect: {}", urlBounce);
                response.sendRedirect(urlBounce);
            }
        }
        catch (IOException e) {
            log.error("Error redirecting to login page", e);
        }
    }

    private void updateUser(User u, String pw) {
        u.setPassword(pw);
        u.setLastLoggedIn(new Date());
        UserManager.storeUser(u);
    }

}
