package com.redhat.rhn.common.util.test;

import com.redhat.rhn.common.util.UserPasswordUtils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserPasswordUtilsTest {

    @Test
    public void testPasswordMatcher() throws Exception {
        Map<String, String> errors = new HashMap<>();
        UserPasswordUtils.validatePassword(errors, "allower");
        errors.forEach((k, i) -> System.out.println("k: " + k + " , i: " + i));
    }

}
