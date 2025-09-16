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
package com.redhat.rhn.domain.action.script;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionChild;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ScriptAction
 */
@Entity
@Table(name = "rhnActionScript")
public class ScriptActionDetails extends ActionChild {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "script_action_seq")
    @SequenceGenerator(name = "script_action_seq", sequenceName = "RHN_ACTSCRIPT_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String username = "";
    @Column(nullable = false)
    private String groupname = "";
    @Column(name = "script", columnDefinition = "bytea")
    private byte[] script = {};
    private Long timeout;
    @OneToMany(mappedBy = "parentScriptActionDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScriptResult> results = new HashSet<>();

    /**
     * @return Returns the groupname.
     */
    public String getGroupname() {
        return groupname;
    }

    /**
     * @param g The groupname to set.
     */
    public void setGroupname(String g) {
        this.groupname = g;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the timeout.
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * @param t The timeout to set.
     */
    public void setTimeout(Long t) {
        this.timeout = t;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param u The username to set.
     */
    public void setUsername(String u) {
        this.username = u;
    }

    /**
     * @return Returns the result.
     */
    public Set<ScriptResult> getResults() {
        return results;
    }

    /**
     * @param r The result to set.
     */
    public void setResults(Set<ScriptResult> r) {
        this.results = r;
    }

    /**
     * Add ScriptResult to the results set
     * @param r ScriptResult to add to set
     */
    public void addResult(ScriptResult r) {
        if (this.results == null) { //init results if needed
            this.results = new HashSet<>();
        }
        r.setParentScriptActionDetails(this);
        this.results.add(r);
    }

    /**
     * Get the script
     * @return Returns the script.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * Set the Script contents
     * @param scriptIn The script to set.
     */
    public void setScript(byte[] scriptIn) {
        this.script = scriptIn;
    }

    /**
     * Get the String version of the Script contents
     * @return String version of the Script contents
     */
    public String getScriptContents() {
        return HibernateFactory.getByteArrayContents(this.script);
    }

}
