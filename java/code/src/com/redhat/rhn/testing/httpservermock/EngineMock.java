/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.rhn.testing.httpservermock;

import simple.http.serve.Resource;
import simple.http.serve.ResourceEngine;

/**
 *
 * @author duncan
 */
public class EngineMock implements ResourceEngine {

    ServiceMock service;

    public EngineMock(ServiceMock service) {
        this.service = service;
    }

    public Resource resolve(String string) {
        return this.service;
    }
}
