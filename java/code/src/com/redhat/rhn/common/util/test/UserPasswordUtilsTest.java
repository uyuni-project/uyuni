package com.redhat.rhn.common.util.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.redhat.rhn.common.util.UserPasswordUtils;

public class UserPasswordUtilsTest {

    @Test
    public void testPasswordMatcher() throws Exception {
        
        Map<String, String> errors = new HashMap<>();
        UserPasswordUtils.validatePassword(
            errors,
            "myspecialpassword"
        );
        errors.forEach((k, i) -> {System.out.println("k: " + k + " , i: " + i); });
    }

}
