/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

package com.redhat.rhn.domain.user.legacy;

import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * UserInfo represents the bean version of the DB table
 * RHNUSERINFO
 */
@Entity
@Table(name = "rhnUserInfo")
public class UserInfo extends AbstractUserChild implements Serializable {

    @Id
    @Column(name = "user_id")
    private long id;

    @Column(name = "page_size")
    private int pageSize;

    @Column(name = "email_notify")
    private int emailNotify;

    @Column(name = "tasko_notify", nullable = false)
    @Type(type = "yes_no")
    private boolean taskoNotify;

    @Column(name = "use_pam_authentication")
    @Type(type = "yes_no")
    private boolean usePamAuthentication;

    @Column(name = "show_system_group_list")
    private String showSystemGroupList;

    @Column(name = "preferred_locale")
    private String preferredLocale;

    @Column(name = "preferred_docs_locale")
    private String preferredDocsLocale;

    @Column(name = "last_logged_in")
    private Date lastLoggedIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timezone_id")
    private RhnTimeZone timeZone;

    @OneToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    @Column(name = "csv_separator")
    private char csvSeparator;

    @Column(name = "web_theme")
    private String webTheme;

    /**
     * Create a new empty user
     */
    protected UserInfo() {
    }

    protected void setUser(User u) {
        user = u;
    }

    protected User getUser() {
        return user;
    }

    /**
     * Getter for pageSize
     * @return pageSize
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Setter for pageSize
     * @param pageSizeIn New value for pageSize
     */
    public void setPageSize(int pageSizeIn) {
        this.pageSize = pageSizeIn;
    }

    /**
     * Getter for usePamAuthentication
     * @return usePamAuthentication
     */
    public boolean getUsePamAuthentication() {
        return this.usePamAuthentication;
    }

    /**
     * Setter for usePamAuthentication
     * @param usePamAuthenticationIn New value for usePamAuthentication
     */
    public void setUsePamAuthentication(boolean usePamAuthenticationIn) {
        this.usePamAuthentication = usePamAuthenticationIn;
    }

    /**
     * Getter for showSystemGroupList
     * @return showSystemGroupList
     */
    public String getShowSystemGroupList() {
        if (showSystemGroupList == null || showSystemGroupList.isEmpty()) {
            showSystemGroupList = "N";
        }
        return this.showSystemGroupList;
    }

    /**
     * Setter for showSystemGroupList
     * @param showSystemGroupListIn New value for showSystemGroupList
     */
    public void setShowSystemGroupList(String showSystemGroupListIn) {
        this.showSystemGroupList = showSystemGroupListIn;
    }

    /**
     * Getter for lastLoggedIn
     * @return lastLoggedIn
     */
    public Date getLastLoggedIn() {
        return this.lastLoggedIn;
    }

    /**
     * Setter for lastLoggedIn
     * @param lastLoggedInIn New value for lastLoggedIn
     */
    public void setLastLoggedIn(Date lastLoggedInIn) {
        this.lastLoggedIn = lastLoggedInIn;
    }

    /**
     * @return Returns the timeZone.
     */
    public RhnTimeZone getTimeZone() {
        return timeZone;
    }
    /**
     * @param timeZoneIn The timeZone to set.
     */
    public void setTimeZone(RhnTimeZone timeZoneIn) {
        this.timeZone = timeZoneIn;
    }

    /**
     * Returns user's preferred locale
     * @return locale
     */
    public String getPreferredLocale() {
        return this.preferredLocale;
    }

    /**
     * Sets user's preferred locale
     * @param locale user's preferred locale
     */
    public void setPreferredLocale(String locale) {
        this.preferredLocale = locale;
    }

    /**
     * Returns the user's preferred documentation locale
     * @return String docsLocale
     */
    String getPreferredDocsLocale() {
        return this.preferredDocsLocale;
    }

    /**
     * Sets the user's preferred documentation locale
     * @param docsLocale documentation locale
     */
    void setPreferredDocsLocale(String docsLocale) {
        this.preferredDocsLocale = docsLocale;
    }


    /**
     * Getter for emailNotify
     * @return emailNotify
     */
    public int getEmailNotify() {
        return this.emailNotify;
    }

    /**
     * Setter for emailNotify
     * @param emailNotifyIn New value for emailNotify
     */
    public void setEmailNotify(int emailNotifyIn) {
        this.emailNotify = emailNotifyIn;
    }

    /**
     * Getter for taskoNotify
     * @return taskoNotify
     */
    public boolean getTaskoNotify() {
        return this.taskoNotify;
    }

    /**
     * Setter for taskoNotify
     * @param taskoNotifyIn New value for taskoNotify
     */
    public void settaskoNotify(boolean taskoNotifyIn) {
        this.taskoNotify = taskoNotifyIn;
    }

    /**
     * Getter for csvSeparator
     * @return the csvSeparator
     */
    public char getCsvSeparator() {
        return csvSeparator;
    }

    /**
     * Setter for csvSeparator
     * @param csvSeparatorIn the csvSeparator to set
     */
    public void setCsvSeparator(char csvSeparatorIn) {
        this.csvSeparator = csvSeparatorIn;
    }


    /**
     * Getter for webTheme
     * @return the webTheme
     */
    public String getWebTheme() {
        return this.webTheme;
    }

    /**
     * Setter for webTheme
     * @param webThemeIn the webTheme to set
     */
    public void setWebTheme(String webThemeIn) {
        this.webTheme = webThemeIn;
    }
}
