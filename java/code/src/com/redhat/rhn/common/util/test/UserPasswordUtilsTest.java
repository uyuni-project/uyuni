package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.redhat.rhn.common.util.UserPasswordUtils;
import com.redhat.rhn.domain.common.SatConfigFactory;

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
