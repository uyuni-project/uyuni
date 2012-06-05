package com.redhat.rhn.domain.product;

import java.io.Serializable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

/**
 *
 */
public class SUSEProduct extends BaseDomainHelper implements Serializable {

    private static final long serialVersionUID = 7814344915621295270L;

    private long id;
    private String name;
    private String version;
    private String release;
    private PackageArch arch;
    private String friendlyName;
    private String channelFamilyId;
    private char productList;
    private int productId;

    /**
     * @return the id
     */
    public long getId() {
       return id; 
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
       this.id = id; 
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * @return the release
     */
    public String getRelease() {
        return release;
    }
    
    /**
     * @param release the release to set
     */
    public void setRelease(String release) {
        this.release = release;
    }
    
    /**
     * @return the arch
     */
    public PackageArch getArch() {
        return arch;
    }
    
    /**
     * @param arch the arch to set
     */
    public void setArch(PackageArch arch) {
        this.arch = arch;
    }
    
    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }
    
    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
    
    /**
     * @return the channelFamilyId
     */
    public String getChannelFamilyId() {
        return channelFamilyId;
    }
    
    /**
     * @param channelFamilyId the channelFamilyId to set
     */
    public void setChannelFamilyId(String channelFamilyId) {
        this.channelFamilyId = channelFamilyId;
    }
    
    /**
     * @return the productList
     */
    public char getProductList() {
        return productList;
    }
    
    /**
     * @param productList the productList to set
     */
    public void setProductList(char productList) {
        this.productList = productList;
    }
    
    /**
     * @return the productId
     */
    public int getProductId() {
        return productId;
    }
    
    /**
     * @param productId the productId to set
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }
}
