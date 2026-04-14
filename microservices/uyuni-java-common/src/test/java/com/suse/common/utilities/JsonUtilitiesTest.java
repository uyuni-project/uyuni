/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.common.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

public class JsonUtilitiesTest {
    @Test
    @DisplayName("correctly creates empty json")
    void testCreateEmptyJson() {
        assertEquals("{}", JsonUtilities.createEmptyJson());
    }

    @Test
    @DisplayName("correctly creates simple json")
    void testCreateJson() {
        assertEquals("{\"newKey\": \"newVal\"}", JsonUtilities.createJson("newKey", "newVal"));
    }

    @Test
    @DisplayName("correctly adds json on top of empty json")
    void testAddToJsonWithExistingEmptyJson() {

        assertEquals("{\"newKey\": \"newVal\"}",
                JsonUtilities.addToJson("  ", "newKey", "newVal"));

        assertEquals("{\"newKey\": \"newVal\"}",
                JsonUtilities.addToJson("{}", "newKey", "newVal"));

        assertEquals("{\"newKey\": \"newVal\"}",
                JsonUtilities.addToJson("{   }", "newKey", "newVal"));
    }

    @Test
    @DisplayName("correctly adds json on top of existing json")
    void testAddToJsonWithExistingJson() {
        assertEquals("{\"exKey\": \"exVal\", \"exKey2\": exVal2, \"newKey\": \"newVal\"}",
                JsonUtilities.addToJson("{\"exKey\": \"exVal\", \"exKey2\": exVal2}", "newKey", "newVal"));
    }

    @Test
    @DisplayName("correctly decodes simple json string into map")
    void testDecodeSimpleJsonString() {
        Map<String, String> map = JsonUtilities.decodeSimpleJsonString("""
                {
                  "name": "John Doe",
                  "age": 30,
                  "isStudent": false,
                  "hobby": "reading"
                }
                """);

        assertEquals(4, map.size());

        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("age"));
        assertTrue(map.containsKey("isStudent"));
        assertTrue(map.containsKey("hobby"));
        assertFalse(map.containsKey("dummy"));

        assertEquals("John Doe", map.get("name"));
        assertEquals("30", map.get("age"));
        assertEquals("false", map.get("isStudent"));
        assertEquals("reading", map.get("hobby"));

    }

    @Test
    @DisplayName("correctly decodes complex test json certificate string into map")
    void testDecodeTestCertificateJsonString() throws CertificateException {
        Map<String, String> map = JsonUtilities.decodeSimpleJsonString(TEST_EXAMPLE);

        assertEquals(2, map.size());
        assertTrue(map.containsKey("hostKeyDocument"));
        X509Certificate cert = CertificateHelper.parse(map.get("hostKeyDocument"));

        assertEquals(HOST_KEY_DOCUMENT_STRING, map.get("hostKeyDocument"));

    }

    @Test
    @DisplayName("correctly decodes complex test json Secure Extension Header string into map")
    void testDecodeTestSecureExtensionHeaderJsonString() {
        Map<String, String> map = JsonUtilities.decodeSimpleJsonString(TEST_EXAMPLE);

        assertEquals(2, map.size());

        assertTrue(map.containsKey("secureExtensionHeader"));
        byte[] seh = Base64.getDecoder().decode(map.get("secureExtensionHeader"));
        assertEquals(1040, seh.length);

        char[] signature = "IBMSecEx".toCharArray();
        for (int i = 0; i < signature.length; i++) {
            assertEquals(signature[i], seh[i]);
        }

        assertEquals(SEH_STRING, map.get("secureExtensionHeader"));

    }


    //please keep it as a string concatenation, to avoid \n problems
    private static final String TEST_EXAMPLE =
            "{\"hostKeyDocument\": \"-----BEGIN CERTIFICATE-----\nMIIE9TCCAt2gAwIB" +
                    "AgIJe8PccaVpNh0HMA0GCSqGSIb3DQEBDQUAMIHOMQswCQYD\nVQQGEwJVUzE0MDIG" +
                    "A1UECgwrSW50ZXJuYXRpb25hbCBCdXNpbmVzcyBNYWNoaW5l\ncyBDb3Jwb3JhdGlv" +
                    "bjEpMCcGA1UECwwgSUJNIFoxNiBIb3N0IEtleSBTaWduaW5n\nIFNlcnZpY2UxFTAT" +
                    "BgNVBAcMDFBvdWdoa2VlcHNpZTERMA8GA1UECAwITmV3IFlv\ncmsxNDAyBgNVBAMM" +
                    "K0ludGVybmF0aW9uYWwgQnVzaW5lc3MgTWFjaGluZXMgQ29y\ncG9yYXRpb24wHhcN" +
                    "MjUwNzI0MTI1ODAxWhcNMjcwNzE0MTI1ODAxWjCBsjELMAkG\nA1UEBhMCVVMxKDAm" +
                    "BgNVBAoMH0ludGVybmF0aW9uYWwgQnVzaW5lc3MgTWFjaGlu\nZXMxKzApBgNVBAsM" +
                    "IklCTSBaIEhvc3QgS2V5IFNpZ25pbmcgU2VydmljZSB6MTYx\nDzANBgNVBAcMBkFy" +
                    "bW9uazERMA8GA1UECAwITmV3IFlvcmsxKDAmBgNVBAMMH2li\nbS16LWhvc3Qta2V5" +
                    "LXoxNi0wMDAwMjAwOTY3RjgwgZswEAYHKoZIzj0CAQYFK4EE\nACMDgYYABAF2VvGp" +
                    "rND7keEiJiJcxd5RIm0ESri3ellnO2/dS+WwylmDRvmdCWUu\n3ybnKRq4KFzGhKj5" +
                    "18M9hstng2ja5h27bQAuwihmhmNuHhmFKUii6DwKU99nE5uO\nM8FlKoqPGNvtcaVC" +
                    "dlR6i7NOTRlRhOMvMKrI6Gd6NwCs/iPA8APgM0VVA6N4MHYw\nDgYDVR0PAQH/BAQD" +
                    "AgMIMGQGA1UdHwRdMFswWaBXoFWGU2h0dHBzOi8vd3d3Lmli\nbS5jb20vc3VwcG9y" +
                    "dC9yZXNvdXJjZWxpbmsvYXBpL2NvbnRlbnQvcHVibGljL2li\nbS16LWhvc3Qta2V5" +
                    "LWdlbjIuY3JsMA0GCSqGSIb3DQEBDQUAA4ICAQCYPt5K047O\ndfoUXF7Qbsm9LiS8" +
                    "mwOinU7VcuD7yYJD8FSI9ghocfN7Amm+y9UgaqC+51qRvNBQ\nRzFt08GYASXcoAYf" +
                    "5FzS6MPogKOfK0jWVsGW81l3YuY5II+kUWu3kAwABz/jAXhp\ntRVByux1dwkQ0CFV" +
                    "fUgkQiYi4of7oWTIk2qmRh3Ho2Pbh21FcErFpQGt0HaWyYFK\nntRaMJWXdIVyWBXf" +
                    "mDfeyjfDPcIMHJRjWKgSuepAOLY6hrmnFuDc+uty85n1g5eI\nZlFe6Uyql0FdqB/c" +
                    "RAD9aQn2of8JEgofiEdSzZtPAJLN0l6CEjBVJlsJZeJvRhxv\nJFpYKJjtNMK3NJrh" +
                    "7Xy1x7xYZwK3l03wLWKUZL41BzTGCorr5NOOdrSGv6MHyBhr\n5UKqSTLY6qqzsRMN" +
                    "LcU5W83+crUIDNnO1GA4n4Cdl/HdFZND8bcIuwZGWRi6f+dW\nZ+8nPp2o/uhghscy" +
                    "UIPbyzXoC5mUjauAZUvXozopwrLMNY0fVLDDKo7QqTftEgTA\nsM3cxhERESvauVdz" +
                    "F1O2wzLUi508q0zGZGzK8tR0EMjhhVZNBSt4hduP3e/vWGi+\nNWxXvtoYScvLaQid" +
                    "A5W7Va0S7MeS2/oZgBPQqsPzBPh/cuRnEuFrLNbuZwvSjnGO\n57VBzkLzXz7yqpcw" +
                    "z35dBnjy/xIIxoCIkw==\n-----END CERTIFICATE-----\", " +
                    "\"secureExtensionHeader\": \"SUJNU2VjRXgAAAEAAAAEEImh0IohRiZOaGxFzwAA" +
                    "AAAAAAAAAAAABgAAAAAAAACAAAAAAAAAKOsAAAAAAAAA4AAAAAAAAAAAAAAAAAAAAa" +
                    "RRSkjSjX0Sssg7/TjS2I6/Mz0fVW/bM4Iu80Pe92tOFZ+ccVsOgW4UGmciyVShvDXt" +
                    "mnQf07gft7PMvdy/jF3wAAAAAAAAAAAAAAAAAAABE+dHHdCfTMG3aPxcNweaft0hNW" +
                    "4AJIrfRff0DYoq9tBbN46oVb/rhrfp/SNnrHhDt4/gx5AlI0qFIoADp58Rx0EkIZG2" +
                    "9coRmo4Gbau5TZFup7ZKv/M7KewIFAZE/rTPz8deqwTkHNeSWwLou8tXCHFSRkNYPX" +
                    "LQvV542JHvKYKEgtbbzKaAp0X/zL1OBjidrpEHw16Vxx82FgQH4WmbFDp+EOE6ss6l" +
                    "2vZbHvPXTk0satkl0VgzYTl1KxvNXO4V5H0SNjhjXR1JRTHkWzfQqRIQSbIIIHrWOf" +
                    "MYib1jRaZo6eJg3Sq3vfp59xgfqzCW08SYiayqpbMpYMZz4wyC2JMm/h1D1rLvcASA" +
                    "BUdCJ5R3InlJsomZ8VtRijRSsp45vYUS0BE9shi0o4mhA54C5J9R2vydyiY1hbaPGL" +
                    "8dDdxSQ6tFm7CDpZGIfj2/oO+CvdH3KO/Znh2mUOdYUSYhpl+d2Nd9jTSx27tWkE6z" +
                    "c7g81G2RCgm+xOrovcqwgD4xEZLpVcSOYQSlAcQxxGzHkxFbxfi8g7grU5XGYUTu5Z" +
                    "9vzE+OYUBNroD/my/T4TIz9JHxL0T/Sw3vxGDcehSDpr8wJAJQzBr0cgfNg1Kiwe3I" +
                    "vy9PoegY+2UwJL5AXy6Pq3+4G7m4FeJeL7XjL6bX0SmvQ9fKJzOFLZUaVsleltolgS" +
                    "QVZ0nHnSKEg587i3Cp8jcN4b9xcJuOaPosu/uvrfmic2Dp7l3FcZJ8SGEou1o9W3W3" +
                    "f/w6foCWTVrP3oc97HkPPJgH/uMq6WPwBIh6YU00Yhi5AH/yzJUJ37hgFHu25Y4/8e" +
                    "ubCbauIO9Jo9MQ7PQWjhtO24//WJsh5rnTOn3AJr+OkEcPFN0ZSvWLJbLW7vjZJSAA" +
                    "mPuKomftGUgeWp8KG9X1HWx3FlGnaAFWFRKbFzE1p7mvEPlEFVAtPJ/9g3y3BFg4zu" +
                    "WZZnWCvSJ9r6ZOu9jZQw3sORmqvJbEg5bqFlXYioLtTKqfn4ppOqTM5SqsZ1jJHWor" +
                    "zcmOHgqVsXM09nfOFC06kX/OwqE4vb3w/3Lxms8n4XRHrsO7ODG5DoBdPldxmNRmiG" +
                    "+LAPdlUybbc6LjfNj0cy7Af8KT+izmhVp0bHKQg+tcHqngrAD56zRwXzxfqQL7P14D" +
                    "dFhRmuI69PKkto+bnNMjfyY28q4=\"}";

    private static final String HOST_KEY_DOCUMENT_STRING = """
            -----BEGIN CERTIFICATE-----
            MIIE9TCCAt2gAwIBAgIJe8PccaVpNh0HMA0GCSqGSIb3DQEBDQUAMIHOMQswCQYD
            VQQGEwJVUzE0MDIGA1UECgwrSW50ZXJuYXRpb25hbCBCdXNpbmVzcyBNYWNoaW5l
            cyBDb3Jwb3JhdGlvbjEpMCcGA1UECwwgSUJNIFoxNiBIb3N0IEtleSBTaWduaW5n
            IFNlcnZpY2UxFTATBgNVBAcMDFBvdWdoa2VlcHNpZTERMA8GA1UECAwITmV3IFlv
            cmsxNDAyBgNVBAMMK0ludGVybmF0aW9uYWwgQnVzaW5lc3MgTWFjaGluZXMgQ29y
            cG9yYXRpb24wHhcNMjUwNzI0MTI1ODAxWhcNMjcwNzE0MTI1ODAxWjCBsjELMAkG
            A1UEBhMCVVMxKDAmBgNVBAoMH0ludGVybmF0aW9uYWwgQnVzaW5lc3MgTWFjaGlu
            ZXMxKzApBgNVBAsMIklCTSBaIEhvc3QgS2V5IFNpZ25pbmcgU2VydmljZSB6MTYx
            DzANBgNVBAcMBkFybW9uazERMA8GA1UECAwITmV3IFlvcmsxKDAmBgNVBAMMH2li
            bS16LWhvc3Qta2V5LXoxNi0wMDAwMjAwOTY3RjgwgZswEAYHKoZIzj0CAQYFK4EE
            ACMDgYYABAF2VvGprND7keEiJiJcxd5RIm0ESri3ellnO2/dS+WwylmDRvmdCWUu
            3ybnKRq4KFzGhKj518M9hstng2ja5h27bQAuwihmhmNuHhmFKUii6DwKU99nE5uO
            M8FlKoqPGNvtcaVCdlR6i7NOTRlRhOMvMKrI6Gd6NwCs/iPA8APgM0VVA6N4MHYw
            DgYDVR0PAQH/BAQDAgMIMGQGA1UdHwRdMFswWaBXoFWGU2h0dHBzOi8vd3d3Lmli
            bS5jb20vc3VwcG9ydC9yZXNvdXJjZWxpbmsvYXBpL2NvbnRlbnQvcHVibGljL2li
            bS16LWhvc3Qta2V5LWdlbjIuY3JsMA0GCSqGSIb3DQEBDQUAA4ICAQCYPt5K047O
            dfoUXF7Qbsm9LiS8mwOinU7VcuD7yYJD8FSI9ghocfN7Amm+y9UgaqC+51qRvNBQ
            RzFt08GYASXcoAYf5FzS6MPogKOfK0jWVsGW81l3YuY5II+kUWu3kAwABz/jAXhp
            tRVByux1dwkQ0CFVfUgkQiYi4of7oWTIk2qmRh3Ho2Pbh21FcErFpQGt0HaWyYFK
            ntRaMJWXdIVyWBXfmDfeyjfDPcIMHJRjWKgSuepAOLY6hrmnFuDc+uty85n1g5eI
            ZlFe6Uyql0FdqB/cRAD9aQn2of8JEgofiEdSzZtPAJLN0l6CEjBVJlsJZeJvRhxv
            JFpYKJjtNMK3NJrh7Xy1x7xYZwK3l03wLWKUZL41BzTGCorr5NOOdrSGv6MHyBhr
            5UKqSTLY6qqzsRMNLcU5W83+crUIDNnO1GA4n4Cdl/HdFZND8bcIuwZGWRi6f+dW
            Z+8nPp2o/uhghscyUIPbyzXoC5mUjauAZUvXozopwrLMNY0fVLDDKo7QqTftEgTA
            sM3cxhERESvauVdzF1O2wzLUi508q0zGZGzK8tR0EMjhhVZNBSt4hduP3e/vWGi+
            NWxXvtoYScvLaQidA5W7Va0S7MeS2/oZgBPQqsPzBPh/cuRnEuFrLNbuZwvSjnGO
            57VBzkLzXz7yqpcwz35dBnjy/xIIxoCIkw==
            -----END CERTIFICATE-----""";

    //please keep it as a string concatenation, to avoid \n problems
    private static final String SEH_STRING =
            "SUJNU2VjRXgAAAEAAAAEEImh0IohRiZOaGxFzwAAAAAAAAAAAAAABgAAAAAAAACAAAAAAAAAKOsA" +
                    "AAAAAAAA4AAAAAAAAAAAAAAAAAAAAaRRSkjSjX0Sssg7/TjS2I6/Mz0fVW/bM4Iu80Pe92tOFZ+c" +
                    "cVsOgW4UGmciyVShvDXtmnQf07gft7PMvdy/jF3wAAAAAAAAAAAAAAAAAAABE+dHHdCfTMG3aPxc" +
                    "Nweaft0hNW4AJIrfRff0DYoq9tBbN46oVb/rhrfp/SNnrHhDt4/gx5AlI0qFIoADp58Rx0EkIZG2" +
                    "9coRmo4Gbau5TZFup7ZKv/M7KewIFAZE/rTPz8deqwTkHNeSWwLou8tXCHFSRkNYPXLQvV542JHv" +
                    "KYKEgtbbzKaAp0X/zL1OBjidrpEHw16Vxx82FgQH4WmbFDp+EOE6ss6l2vZbHvPXTk0satkl0Vgz" +
                    "YTl1KxvNXO4V5H0SNjhjXR1JRTHkWzfQqRIQSbIIIHrWOfMYib1jRaZo6eJg3Sq3vfp59xgfqzCW" +
                    "08SYiayqpbMpYMZz4wyC2JMm/h1D1rLvcASABUdCJ5R3InlJsomZ8VtRijRSsp45vYUS0BE9shi0" +
                    "o4mhA54C5J9R2vydyiY1hbaPGL8dDdxSQ6tFm7CDpZGIfj2/oO+CvdH3KO/Znh2mUOdYUSYhpl+d" +
                    "2Nd9jTSx27tWkE6zc7g81G2RCgm+xOrovcqwgD4xEZLpVcSOYQSlAcQxxGzHkxFbxfi8g7grU5XG" +
                    "YUTu5Z9vzE+OYUBNroD/my/T4TIz9JHxL0T/Sw3vxGDcehSDpr8wJAJQzBr0cgfNg1Kiwe3Ivy9P" +
                    "oegY+2UwJL5AXy6Pq3+4G7m4FeJeL7XjL6bX0SmvQ9fKJzOFLZUaVsleltolgSQVZ0nHnSKEg587" +
                    "i3Cp8jcN4b9xcJuOaPosu/uvrfmic2Dp7l3FcZJ8SGEou1o9W3W3f/w6foCWTVrP3oc97HkPPJgH" +
                    "/uMq6WPwBIh6YU00Yhi5AH/yzJUJ37hgFHu25Y4/8eubCbauIO9Jo9MQ7PQWjhtO24//WJsh5rnT" +
                    "On3AJr+OkEcPFN0ZSvWLJbLW7vjZJSAAmPuKomftGUgeWp8KG9X1HWx3FlGnaAFWFRKbFzE1p7mv" +
                    "EPlEFVAtPJ/9g3y3BFg4zuWZZnWCvSJ9r6ZOu9jZQw3sORmqvJbEg5bqFlXYioLtTKqfn4ppOqTM" +
                    "5SqsZ1jJHWorzcmOHgqVsXM09nfOFC06kX/OwqE4vb3w/3Lxms8n4XRHrsO7ODG5DoBdPldxmNRm" +
                    "iG+LAPdlUybbc6LjfNj0cy7Af8KT+izmhVp0bHKQg+tcHqngrAD56zRwXzxfqQL7P14DdFhRmuI6" +
                    "9PKkto+bnNMjfyY28q4=";

}
