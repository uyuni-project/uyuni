package com.redhat.rhn.manager.distupgrade;

/**
 * Exception thrown in case a product migration is attempted in a SUMA PAYG instace
 */
public class DistUpgradePaygException extends Exception{

    /**
     * Constructor
     */
    DistUpgradePaygException() { super(); }
}
