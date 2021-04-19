package com.suse.scc.model;

public class SCCSystemCredentialsJson {

    private String login;
    private String password;
    private Long id;

    public SCCSystemCredentialsJson(String login, String password, Long id) {
       this.login = login;
       this.password = password;
       this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id;
    }
}
