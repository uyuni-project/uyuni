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
package com.suse.cloud;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * CspBillingAdapter Status
 */
public class CspBillingAdapterStatus {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSz";

    @SerializedName("billing_api_access_ok")
    private boolean billingApiAccessOk;
    @SerializedName("timestamp")
    private Date timestamp;
    @SerializedName("expire")
    private Date expire;
    @SerializedName("errors")
    private List<String> errors;

    /**
     * @return return true if api access was ok
     */
    public boolean isBillingApiAccessOk() {
        return billingApiAccessOk;
    }

    private void setBillingApiAccessOk(Boolean billingApiAccessOkIn) {
        billingApiAccessOk = billingApiAccessOkIn;
    }

    /**
     * @return return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(Date timestampIn) {
        timestamp = timestampIn;
    }

    /**
     * @return return the expire timestamp
     */
    public Date getExpire() {
        return expire;
    }

    private void setExpire(Date expireIn) {
        expire = expireIn;
    }

    /**
     * @return return a list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    private void setErrors(List<String> errorsIn) {
        errors = errorsIn;
    }
}
