
package com.redhat.rhn.domain.action.errata;

import com.redhat.rhn.domain.action.ActionChild;

public class ActionPackageDetails extends ActionChild {
     private Long id;
     private boolean allowVendorChange = false;

     public ActionPackageDetails() {     }

     public ActionPackageDetails(boolean allowVendorChangeIn) {
         this.allowVendorChange = allowVendorChangeIn;
     }

    /**
     * Return the ID.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     * @param idIn id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }


    /**
     * @return Returns allow vendor change
     */
    public boolean getAllowVendorChange() {
        return allowVendorChange;
    }

    /**
     * @param allowVendorChangeIn Set allow vendor change flag.
     */
    public void setAllowVendorChange(boolean allowVendorChangeIn) {
        this.allowVendorChange = allowVendorChangeIn;
    }
}