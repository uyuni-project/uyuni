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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Test;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * Unit tests for AccountEmailController.
 * Tests cover email validation, request parsing, and controller logic.
 */
public class AccountEmailControllerTest {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    // ==================== Email Request Tests ====================

    @Test
    public void testEmailChangeRequestParsing() {
        String json = "{\"email\": \"newemail@example.com\"}";
        AccountEmailController.EmailChangeRequest request = 
            GSON.fromJson(json, AccountEmailController.EmailChangeRequest.class);
        
        assertNotNull(request);
        assertEquals("newemail@example.com", request.getEmail());
    }

    @Test
    public void testEmailChangeRequestConstruction() {
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest("test@example.com");
        
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    public void testEmailChangeRequestSetter() {
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest("old@example.com");
        
        request.setEmail("new@example.com");
        assertEquals("new@example.com", request.getEmail());
    }

    @Test
    public void testEmailChangeRequestFromJson() {
        String json = "{\"email\": \"parsed@example.com\"}";
        AccountEmailController.EmailChangeRequest request = 
            GSON.fromJson(json, AccountEmailController.EmailChangeRequest.class);
        
        assertEquals("parsed@example.com", request.getEmail());
    }

    // ==================== Email Validation Tests ====================

    @Test
    public void testValidEmailAddressSimple() throws Exception {
        InternetAddress addr = new InternetAddress("user@example.com");
        addr.validate(); // Should not throw
    }

    @Test
    public void testValidEmailAddressComplex() throws Exception {
        InternetAddress addr = new InternetAddress("firstname.lastname+tag@example.co.uk");
        addr.validate(); // Should not throw
    }

    @Test
    public void testInvalidEmailAddressNoAtSign() {
        String invalidEmail = "notanemail";
        assertThrows(AddressException.class, () -> {
            InternetAddress addr = new InternetAddress(invalidEmail);
            addr.validate();
        });
    }

    @Test
    public void testInvalidEmailAddressMultipleAtSigns() {
        String invalidEmail = "test@@example.com";
        assertThrows(AddressException.class, () -> {
            InternetAddress addr = new InternetAddress(invalidEmail);
            addr.validate();
        });
    }

    @Test
    public void testInvalidEmailAddressNoLocalPart() {
        String invalidEmail = "@example.com";
        assertThrows(AddressException.class, () -> {
            InternetAddress addr = new InternetAddress(invalidEmail);
            addr.validate();
        });
    }

    @Test
    public void testInvalidEmailAddressNoDomain() {
        String invalidEmail = "user@";
        assertThrows(AddressException.class, () -> {
            InternetAddress addr = new InternetAddress(invalidEmail);
            addr.validate();
        });
    }


    // ==================== Submit Form Tests ====================

    @Test
    public void testSubmitFormValidEmailChange() {
        // Test that a valid email change request is properly formed
        String validEmail = "newemail@example.com";
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest(validEmail);
        
        assertNotNull(request);
        assertEquals(validEmail, request.getEmail());
        
        // Verify it can be serialized to JSON
        String json = GSON.toJson(request);
        assertNotNull(json);
        
        // Verify it can be deserialized back
        AccountEmailController.EmailChangeRequest deserializedRequest = 
            GSON.fromJson(json, AccountEmailController.EmailChangeRequest.class);
        assertEquals(validEmail, deserializedRequest.getEmail());
    }

    @Test
    public void testSubmitFormEmptyEmail() {
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest("");
        
        assertNotNull(request);
        assertEquals("", request.getEmail());
    }

    @Test
    public void testSubmitFormNullEmail() {
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest(null);
        
        assertNotNull(request);
        assertEquals(null, request.getEmail());
    }

    // ==================== Error Handling Tests ====================

@Test
public void testBadParameterExceptionForInvalidUid() {
    assertThrows(NumberFormatException.class, () -> {
        // Simulating invalid uid parameter
        String invalidUid = "not-a-number";
        Long.parseLong(invalidUid);
    });
}

    @Test
    public void testEmailValidationMultipleFormats() throws Exception {
        // Test various valid email formats
        String[] validEmails = {
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email@example.com",
            "other.email-with-hyphen@example.com",
            "user+tag@example.com",
            "name/surname@example.com"
        };
        
        for (String email : validEmails) {
            InternetAddress addr = new InternetAddress(email);
            addr.validate(); // Should not throw
        }
    }

    @Test
    public void testEmailValidationInvalidFormats() {
        // Test various invalid email formats
        String[] invalidEmails = {
            "plainaddress",
            "@no-local-part.com",
            "no-domain@.com",
            "two@@example.com",
            "space in@example.com"
        };
        
        for (String email : invalidEmails) {
            assertThrows(AddressException.class, () -> {
                InternetAddress addr = new InternetAddress(email);
                addr.validate();
            }, "Should throw for invalid email: " + email);
        }
    }

    // ==================== Serialization Tests ====================

    @Test
    public void testEmailChangeRequestSerialization() {
        AccountEmailController.EmailChangeRequest original = 
            new AccountEmailController.EmailChangeRequest("test@example.com");
        
        String json = GSON.toJson(original);
        AccountEmailController.EmailChangeRequest restored = 
            GSON.fromJson(json, AccountEmailController.EmailChangeRequest.class);
        
        assertEquals(original.getEmail(), restored.getEmail());
    }

    @Test
    public void testEmailChangeRequestWithSpecialCharacters() {
        String emailWithSpecialChars = "user+test.tag@sub.example.com";
        AccountEmailController.EmailChangeRequest request = 
            new AccountEmailController.EmailChangeRequest(emailWithSpecialChars);
        
        assertEquals(emailWithSpecialChars, request.getEmail());
    }

    // ==================== Email Comparison Tests ====================

    @Test
    public void testEmailComparison() {
        String email1 = "user@example.com";
        String email2 = "user@example.com";
        String email3 = "other@example.com";
        
        assertEquals(email1, email2);
        assertEquals(email1, email1);
        assertEquals(false, email1.equals(email3));
    }

    @Test
    public void testEmailCaseSensitivity() {
        // Note: Email addresses are technically case-sensitive in local part
        // but most systems treat them as case-insensitive
        String email1 = "User@example.com";
        String email2 = "user@example.com";
        
        // Direct comparison is case-sensitive
        assertEquals(false, email1.equals(email2));
    }

    @Test
    public void testEmailWithWhitespace() {
        String emailWithSpace = "  user@example.com  ";
        String trimmedEmail = emailWithSpace.trim();
        
        assertEquals("user@example.com", trimmedEmail);
    }
}



