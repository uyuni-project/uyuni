package com.redhat.rhn.common.logging;

import java.util.Map;

public class AuditLogMessage {

    private String uid;
    private String message;
    private String host;
    private Map<String, String> extmap;

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUid() {
        return uid;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    public void setExtmap(Map<String, String> extmap) {
        this.extmap = extmap;
    }
    public Map<String, String> getExtmap() {
        return extmap;
    }
}
