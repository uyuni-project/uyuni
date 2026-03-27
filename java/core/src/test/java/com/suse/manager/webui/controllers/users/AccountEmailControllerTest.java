/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.webui.controllers.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for AccountEmailController.
 * Tests focus on controller request/response handling and email validation logic.
 */
public class AccountEmailControllerTest {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    @Test
    public void testEmailChangeRequestParsing() {
        String json = "{\"email\": \"newemail@example.com\"}";
        AccountEmailController.EmailChangeRequest request = 
            GSON.fromJson(json, AccountEmailController.EmailChangeRequest.class);
        
        assertNotNull(request);
        assertEquals("newemail@example.com", request.getEmail());
    }

    @Test
    public void testValidEmailAddress() throws Exception {
        // Test that valid email addresses don't throw exceptions
        jakarta.mail.internet.InternetAddress addr = 
            new jakarta.mail.internet.InternetAddress("valid.email@example.co.uk");
        addr.validate(); // Should not throw
    }

    @Test
    public void testInvalidEmailAddress() {
        // Test that invalid email addresses are caught
        String invalidEmail = "not-an-email";
        jakarta.mail.internet.AddressException exception = null;
        
        try {
            jakarta.mail.internet.InternetAddress addr = 
                new jakarta.mail.internet.InternetAddress(invalidEmail);
            addr.validate();
        } catch (jakarta.mail.internet.AddressException e) {
            exception = e;
        }
        
        assertNotNull(exception, "Expected AddressException for invalid email");
    }

    @Test
    public void testEmailChangeRequest() {
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest("new@example.com");
        
        assertNotNull(request);
        assertEquals("new@example.com", request.getEmail());
        
        request.setEmail("another@example.com");
        assertEquals("another@example.com", request.getEmail());
    }
}


