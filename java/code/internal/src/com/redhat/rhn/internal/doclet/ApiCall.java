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
package com.redhat.rhn.internal.doclet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.ExecutableElement;

/**
 *
 * ApiCall
 */
public class ApiCall implements Comparable<ApiCall> {

    private String name;
    private String doc;
    private List<String> params = new ArrayList<>();
    private String returnDoc;
    private boolean deprecated = false;
    private String deprecatedVersion;
    private boolean sinceAvailable = false;
    private String sinceVersion;
    private ExecutableElement method;
    private boolean ignored = false;

    /**
     * constructor
     * @param meth the method to set
     */
    public ApiCall(ExecutableElement meth) {
        method = meth;
    }

    /**
     * constructor
     *
     */
    public ApiCall() {

    }

    /**
     * Gets the deprecated version
     * @return the deprecated version
     */
    public String getDeprecatedReason() {
        return deprecatedVersion;
    }

    /**
     * sets the deprecated version
     * @param deprecatedVersionIn  the version
     */
    public void setDeprecatedReason(String deprecatedVersionIn) {
        this.deprecatedVersion = deprecatedVersionIn;
    }

    /**
     * is the call deprecated
     * @return true of it's deprecated
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Sets if the call is deprecated
     * @param deprecatedIn is it deprecated?
     */
    public void setDeprecated(boolean deprecatedIn) {
        this.deprecated = deprecatedIn;
    }

    /**
     * Gets the since version. (i.e API version that the API was introduced)
     * @return the since version
     */
    public String getSinceVersion() {
        return sinceVersion;
    }

    /**
     * sets the since version
     * @param sinceVersionIn  the version
     */
    public void setSinceVersion(String sinceVersionIn) {
        this.sinceVersion = sinceVersionIn;
    }

    /**
     * did the call include a tag to indicate when the API was made available?
     * @return true of it was available
     */
    public boolean isSinceAvailable() {
        return sinceAvailable;
    }

    /**
     * Sets if the API included a 'since' tag
     * @param sinceAvailableIn since tag included?
     */
    public void setSinceAvailable(boolean sinceAvailableIn) {
        this.sinceAvailable = sinceAvailableIn;
    }
    /**
     * gets the call's name (namespace)
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the call's name
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Get the calls unique ID to be used in the generated pages.
     * @return the ID
     */
    public String getId() {
        return name + "-" + getMethod().hashCode();
    }

    /**
     * gets the call's params
     * @return an array of the calls params
     */
    public List<String> getParams() {
        return params;
    }

    /**
     * sets the call's params
     * @param paramsIn the aprams to set
     */
    public void setParams(List<String> paramsIn) {
        this.params = paramsIn;
    }

    /**
     * adds a param to teh call's list
     * @param param the param
     */
    public void addParam(String param) {
        this.params.add(param);
    }

    /**
     * gets the return documentatino of the call
     * @return the return docs
     */
    public String getReturnDoc() {
        return returnDoc;
    }

    /**
     * Sets the return documentation
     * @param returnDocIn the return doc
     */
    public void setReturnDoc(String returnDocIn) {
        this.returnDoc = returnDocIn;
    }

    /**
     * gets a description of the api call
     * @return a description
     */
    public String getDoc() {
        return doc;
    }

    /**
     * sets the description
     * @param docIn the description
     */
    public void setDoc(String docIn) {
        this.doc = docIn;
    }


    /**
     * Get the method
     * @return the method
     */
    public ExecutableElement getMethod() {
        return method;
    }



    /**
     * Set the method
     * @param methodIn the method to set
     */
    public void setMethod(ExecutableElement methodIn) {
        this.method = methodIn;
    }

    /**
     * gets whether to ignore the handler
     * @return true if ignored
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * flags the handler as ignored
     */
    public void setIgnored() {
        this.ignored = true;
    }


    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        ApiCall apiCall = (ApiCall) oIn;
        return Objects.equals(name, apiCall.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Override
    public int compareTo(ApiCall o) {
        return this.getName().compareTo(o.getName());
    }
}
