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
package com.suse.manager.webui.controllers;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class SSOTestUtils {

    // This private key and certificate are for testing purposes only and do not represent any real credentials.
    private static final String SSO_KEY = """
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXaY27ke5s13BT
        WSbAGYBU68e0BIqfl+I7AgbOWER9hcFHwCJmrFyi+I3OJUNV/bGqOyFieWgIeBZb
        k3NU7zz/EDk+NA2ixJmNPNMEU7Fl3iXm2W3boR/8fjQqojmzLHui6/8SvtUbWarP
        5hD7ghyJievSlYPSWzhGwMIiPM2lvuFN1hosU64+l7Un6kEIY8V54j3M84xQc3Or
        zcsO4ATo0fqwgoDw5864rbhTSz1WNDt25WVVamThbSw0HEDBh3RcupooUGo2bcye
        BBa3683sl9K88yjUJztaNCUhYrZdzzKs74rgaUjR8XBeQo+uUcQGVT75p21jmYSd
        shkCGdUXAgMBAAECggEAGo4So4D2lfQ66QNLvok6bqpjffkF7vOOY9b5Jptod7NL
        sK/L2MIGBTced7clViGGVVrFgiXUzxdwpGL56T2ELx/DSluKwK9GVvUB9VJQXJNX
        hmM/1zMtDvV7ZLbK89erX2mQdNMvQPq68HdjJxkstBqSV36Nfgktl4sbATI0xwqr
        /Tct/FGC3PcRtFzsTYcgNA0MkLix7vOL2UXFm6JSBoQJ/cEdAMUVj+ipcWnOrYxL
        5YzUAP8MMOpHUH5AHBYLN8D5mja/BPxTtcNUiBLz4Zgvk5XtXGaeH5bHB+n1BzTp
        rDj2HkEMu1wfAjB7pEkM1iQmSW5KlbinWGEkAQjywQKBgQDRO4OyVmYKq8fB3G2M
        jO7lLORO/K+rm4pRsjfewLo1xZqmgY/2YO1zSill3hDda5KP6pasuMAfUfUQCoZh
        DiVjXmamm3Crwg6BwI/uFgaAzw4ROEkYBy4d/wMCBBDH3Gu2RW98pj7XxiANUz2N
        eUQ7NLowuGJTCw75mkJhaqOOMQKBgQC5QYN3qQnD26UVS5tWTdrvSURM2mSxXP2P
        oF7Yil/FDUEHQkFedyoaG01HaLpC4zchgbCNRgvX5DpSLaYTJIYeZ/CV+rbS/uvz
        MtLEBMCkRxjaVbmXOukcgVjQHKE+y0pBACRJC0dI5uppt8a9Dmq9+pwSGF1hLA7F
        98yhIxbdxwKBgQCU0BN2/+RLqqnExBZWZNZ0wV0QoMAA+fuC55K9J65JGGZKDtRp
        k77OxOx0u2CbWys+mMbZyf07SXtXEWVKGlmVN0sjLGMShk7zEhZMa/XsH1gN/05d
        fJ3cT8e/40xcGfWyCeila0g/B3c8gvvAZ4OT6IiUpk+oaLEZ7hFQYBw7wQKBgBG/
        xB/H00avmZ5zsvtO1EYOx/txBSq1FRoYrF2kzE4t2egfIfIyCpebvAi2cPoNmO+d
        5FN0vZe+pxPOidXVrv/kx3knHzMR93tCiEz9g5N5uFja08A4hbMUdXTi+VPx2Dho
        EhHpgbfrX9QnJmRgqDtxcliNewzfUr8G4dUiboNrAoGAeST2Fp7oG7xa8MoQWfGQ
        +bAG1vhy0Agpgkvrx6d9D9cFYRMmWdpSiSF6us30FiGTTB2MMUUvWV5VWWw4gP3Q
        EGUFJ/awJeo6nji0RfjmnchnTGneGD0CJLJfxdtj2UVwoweo+1paVCD5aLUBp4Ka
        qq/3nFZviSem2DspIpdiT9E=
        """;

    private static final String SSO_CERTIFICATE = """
        MIIC/zCCAeegAwIBAgIUAvpjLRo8wMdjPNX1BwihjgM62EUwDQYJKoZIhvcNAQEL
        BQAwDzENMAsGA1UEAwwEVGVzdDAeFw0yNjA0MjEwNzM3MDVaFw0zNjA0MTgwNzM3
        MDVaMA8xDTALBgNVBAMMBFRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
        AoIBAQCXaY27ke5s13BTWSbAGYBU68e0BIqfl+I7AgbOWER9hcFHwCJmrFyi+I3O
        JUNV/bGqOyFieWgIeBZbk3NU7zz/EDk+NA2ixJmNPNMEU7Fl3iXm2W3boR/8fjQq
        ojmzLHui6/8SvtUbWarP5hD7ghyJievSlYPSWzhGwMIiPM2lvuFN1hosU64+l7Un
        6kEIY8V54j3M84xQc3OrzcsO4ATo0fqwgoDw5864rbhTSz1WNDt25WVVamThbSw0
        HEDBh3RcupooUGo2bcyeBBa3683sl9K88yjUJztaNCUhYrZdzzKs74rgaUjR8XBe
        Qo+uUcQGVT75p21jmYSdshkCGdUXAgMBAAGjUzBRMB0GA1UdDgQWBBTv9Fa/HZlo
        g7+M9FoadvIiwqJHojAfBgNVHSMEGDAWgBTv9Fa/HZlog7+M9FoadvIiwqJHojAP
        BgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBUz+GAAcOozRWfWsuN
        fYXGTFmD7Js2PG+c+Fm86NTn6cox7voIG3JPJWilDQRZA/26AcGyU0/SReYG+Rjm
        rSJdBu7IWmNc6Zpjes/7LrHW62duzePGrgIwpDVCoDvOdv7OsrNHqOGEezF2c5Hm
        1qebSqNQxxJ/yS9w8ozOJlWvWmyD6ZSvnpp8UBbssy2cHUL9MtXbufj3rGA+AR8Q
        qULtoWgJ4dg+MdEVd0rrD7mZAAk1ie/k9/fHiFZXN2ReV6oEG/v2ul9znJR7AXkC
        xetLH3GWoOuIJvT23kSYUMS7oSDkBsn2Z7c0LV/V3c7uPqgW/W10VvrE5kxarsIY
        YboZ
        """;

    private SSOTestUtils() {
        // Prevent instantiation
    }

    /**
     * Generates a mock SAML 2.0 configuration for testing purposes.
     * <p>
     * This method constructs a {@link Saml2Settings} instance pre-populated with
     * dummy Service Provider (SP) and Identity Provider (IdP) parameters, including
     * entity IDs, endpoint URLs, XML validation flags, and a predefined X.509 certificate.
     *
     * @return an {@link Optional} containing the configured test {@link Saml2Settings} instance
     */
    public static Saml2Settings getSaml2Settings() {
        Map<String, Object> samlData = new HashMap<>();
        SettingsBuilder builder = new SettingsBuilder();
        samlData.put("onelogin.saml2.sp.entityid", "https://localhost/metadata.jsp");
        samlData.put("onelogin.saml2.sp.assertion_consumer_service.url", "https://localhost/acs.jsp");
        samlData.put("onelogin.saml2.security.want_xml_validation", true);
        samlData.put("onelogin.saml2.idp.entityid", "https://idp");
        samlData.put("onelogin.saml2.idp.single_logout_service.url", "https://idp/slo");
        samlData.put("onelogin.saml2.idp.single_sign_on_service.url", "https://idp/sso");
        samlData.put("onelogin.saml2.idp.x509cert", SSO_CERTIFICATE);
        samlData.put("onelogin.saml2.sp.x509cert", SSO_CERTIFICATE);
        samlData.put("onelogin.saml2.sp.privatekey", SSO_KEY);
        samlData.put("onelogin.saml2.security.signature_algorithm", Constants.RSA_SHA256);
        return builder.fromValues(samlData).build();
    }

    /**
     * Retrieves the SSO certificate for unit tests
     * @return the {@link X509Certificate} instance
     * @throws GeneralSecurityException if an error occurs
     */
    public static X509Certificate getTestCertificate() throws GeneralSecurityException {
        try (InputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(SSO_CERTIFICATE))) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(inputStream);
        }
        catch (IOException ex) {
            throw new GeneralSecurityException("Unable to read certificate", ex);
        }
    }

    /**
     * Retrieves the SSO private key for unit tests
     * @return the {@link PrivateKey} instante
     * @throws GeneralSecurityException if an error occurs
     */
    public static PrivateKey getTestKey() throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(SSO_KEY)));
    }

    /**
     * Sign a SAML xml document with the test certificate/key pair.
     * @param xml the SAML XML document as a string
     * @param elementId the id of the element to sign
     * @return the signed document as a string
     */
    public static String signSamlDocument(String xml, String elementId) {
        try {
            PrivateKey privateKey = getTestKey();
            X509Certificate cert = getTestCertificate();

            Document document = parseUnsignedXml(xml);

            // Specify the ID attribute is user-defined
            document.getDocumentElement().setIdAttribute("ID", true);

            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");

            DigestMethod digestMethod = signatureFactory.newDigestMethod(DigestMethod.SHA256, null);
            Transform envelope = signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            Reference ref = signatureFactory.newReference("#" + elementId, digestMethod, List.of(envelope), null, null);

            CanonicalizationMethod c14nMethod = signatureFactory.newCanonicalizationMethod(
                    CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null
            );
            SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA256, null);
            SignedInfo signedInfo = signatureFactory.newSignedInfo(c14nMethod, signatureMethod, List.of(ref));

            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
            X509Data certificateData = keyInfoFactory.newX509Data(List.of(cert));
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(List.of(certificateData));

            // Specify where to put the signature
            DOMSignContext signContext;
            NodeList issuer = document.getElementsByTagName("saml:Issuer");
            if (issuer.getLength() > 0) {
                // Find the next sibling element after the Issuer element to insert the signature before it
                Node nextSibling = issuer.item(0).getNextSibling();
                while (nextSibling.getNodeType() != Node.ELEMENT_NODE) {
                    nextSibling = nextSibling.getNextSibling();
                }

                signContext = new DOMSignContext(privateKey, document.getDocumentElement(), nextSibling);
            }
            else {
                signContext = new DOMSignContext(privateKey, document.getDocumentElement());
            }

            XMLSignature signature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(signContext);

            return serializeSignedXml(document);
        }
        catch (GeneralSecurityException | MarshalException | XMLSignatureException e) {
            throw new IllegalStateException("Error signing XML", e);
        }
    }

    private static String serializeSignedXml(Document doc) throws MarshalException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();
        }
        catch (TransformerException e) {
            throw new MarshalException("Unable to serialize XML", e);
        }
    }

    private static Document parseUnsignedXml(String xml) throws MarshalException {
        try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            return dbf.newDocumentBuilder().parse(inputStream);
        }
        catch (IOException | SAXException | ParserConfigurationException e) {
            throw new MarshalException("Unable to parse the given XML", e);
        }
    }
}
