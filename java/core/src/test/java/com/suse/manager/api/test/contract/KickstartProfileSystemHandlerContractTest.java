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
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class KickstartProfileSystemHandlerContractTest extends BaseOpenApiTest {

    private static final String KS_LABEL = "test-profile";

    @Override
    protected String getApiNamespace() {
        return "kickstart.profile.system";
    }

    @Override
    protected Class<SystemDetailsHandler> getHandlerClass() {
        return SystemDetailsHandler.class;
    }

    private SystemDetailsHandler handler() {
        return (SystemDetailsHandler) handlerMock;
    }

    /**
     * Builds a crypto key serialized by the registered CryptoKeySerializer.
     *
     * @return the crypto key
     */
    private CryptoKey cryptoKey() {
        CryptoKey key = context.mock(CryptoKey.class, "cryptoKey");
        CryptoKeyType type = context.mock(CryptoKeyType.class, "cryptoKeyType");

        context.checking(new Expectations() {{
            allowing(type).getLabel();
            will(returnValue("GPG"));
            allowing(key).getDescription();
            will(returnValue("test-key"));
            allowing(key).getCryptoKeyType();
            will(returnValue(type));
            allowing(key).getKeyString();
            will(returnValue("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
        }});

        return key;
    }

    /**
     * Builds a file preservation list serialized by the registered FileListSerializer.
     *
     * @return the file list
     */
    private FileList fileList() {
        FileList list = context.mock(FileList.class, "fileList");
        ConfigFileName fileName = context.mock(ConfigFileName.class, "configFileName");

        context.checking(new Expectations() {{
            allowing(fileName).getPath();
            will(returnValue("/etc/hosts"));
            allowing(list).getLabel();
            will(returnValue("test-file-list"));
            allowing(list).getFileNames();
            will(returnValue(Set.of(fileName)));
        }});

        return list;
    }

    @Test
    public void testCheckConfigManagement() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).checkConfigManagement(with(mockUser), with(KS_LABEL));
            will(returnValue(true));
        }});

        validateApiContract("/kickstart.profile.system/checkConfigManagement", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("checkConfigManagement", User.class, String.class);
    }

    @Test
    public void testEnableConfigManagement() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).enableConfigManagement(with(mockUser), with(KS_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/enableConfigManagement", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("enableConfigManagement", User.class, String.class);
    }

    @Test
    public void testDisableConfigManagement() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).disableConfigManagement(with(mockUser), with(KS_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/disableConfigManagement", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("disableConfigManagement", User.class, String.class);
    }

    @Test
    public void testCheckRemoteCommands() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).checkRemoteCommands(with(mockUser), with(KS_LABEL));
            will(returnValue(true));
        }});

        validateApiContract("/kickstart.profile.system/checkRemoteCommands", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("checkRemoteCommands", User.class, String.class);
    }

    @Test
    public void testEnableRemoteCommands() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).enableRemoteCommands(with(mockUser), with(KS_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/enableRemoteCommands", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("enableRemoteCommands", User.class, String.class);
    }

    @Test
    public void testDisableRemoteCommands() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).disableRemoteCommands(with(mockUser), with(KS_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/disableRemoteCommands", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("disableRemoteCommands", User.class, String.class);
    }

    @Test
    public void testGetSELinux() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getSELinux(with(mockUser), with(KS_LABEL));
            will(returnValue("enforcing"));
        }});

        validateApiContract("/kickstart.profile.system/getSELinux", "GET")
                .withParams(Map.of("ksLabel", new String[]{KS_LABEL}))
                .onHandlerMethod("getSELinux", User.class, String.class);
    }

    @Test
    public void testSetSELinux() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setSELinux(with(mockUser), with(KS_LABEL), with("enforcing"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/setSELinux", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "enforcingMode", "enforcing"))
                .onHandlerMethod("setSELinux", User.class, String.class, String.class);
    }

    /**
     * useUtc documents both boolean values, but the generated schema carries the enum [false, false]
     * because the documented values are copied onto the boolean schema as the strings the annotation
     * declares, so only false validates. See the note on testSetLocale.
     */
    @Test
    public void testGetLocale() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getLocale(with(mockUser), with(KS_LABEL));
            will(returnValue(Map.of("locale", "Europe/Berlin", "useUtc", false)));
        }});

        validateApiContract("/kickstart.profile.system/getLocale", "GET")
                .withParams(Map.of("ksLabel", new String[]{KS_LABEL}))
                .onHandlerMethod("getLocale", User.class, String.class);
    }

    /**
     * The request is sent with useUtc false, the only value the generated schema accepts today:
     * UyuniSwaggerReader#applyDocumentedValues copies the annotation's allowableValues onto the
     * property schema without casting them to its type, so a boolean property documenting options
     * ends up with the enum [false, false] and rejects true.
     */
    @Test
    public void testSetLocale() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setLocale(with(mockUser), with(KS_LABEL), with("Europe/Berlin"), with(false));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/setLocale", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "locale", "Europe/Berlin", "useUtc", false))
                .onHandlerMethod("setLocale", User.class, String.class, String.class, Boolean.class);
    }

    @Test
    public void testGetPartitioningScheme() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPartitioningScheme(with(mockUser), with(KS_LABEL));
            will(returnValue(List.of("part /boot --fstype=ext4 --size=200", "part / --size=1024")));
        }});

        validateApiContract("/kickstart.profile.system/getPartitioningScheme", "GET")
                .withParams(Map.of("ksLabel", new String[]{KS_LABEL}))
                .onHandlerMethod("getPartitioningScheme", User.class, String.class);
    }

    @Test
    public void testSetPartitioningScheme() throws Exception {
        var scheme = List.of("part /boot --fstype=ext4 --size=200", "part / --size=1024");

        context.checking(new Expectations() {{
            oneOf(handler()).setPartitioningScheme(with(mockUser), with(KS_LABEL), with(scheme));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/setPartitioningScheme", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "scheme", scheme))
                .onHandlerMethod("setPartitioningScheme", User.class, String.class, List.class);
    }

    @Test
    public void testListKeys() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listKeys(with(mockUser), with(KS_LABEL));
            will(returnValue(Set.of(cryptoKey())));
        }});

        validateApiContract("/kickstart.profile.system/listKeys", "GET")
                .withParams(Map.of("ksLabel", new String[]{KS_LABEL}))
                .onHandlerMethod("listKeys", User.class, String.class);
    }

    @Test
    public void testAddKeys() throws Exception {
        var descriptions = List.of("test-key");

        context.checking(new Expectations() {{
            oneOf(handler()).addKeys(with(mockUser), with(KS_LABEL), with(descriptions));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/addKeys", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "descriptions", descriptions))
                .onHandlerMethod("addKeys", User.class, String.class, List.class);
    }

    @Test
    public void testRemoveKeys() throws Exception {
        var descriptions = List.of("test-key");

        context.checking(new Expectations() {{
            oneOf(handler()).removeKeys(with(mockUser), with(KS_LABEL), with(descriptions));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/removeKeys", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "descriptions", descriptions))
                .onHandlerMethod("removeKeys", User.class, String.class, List.class);
    }

    @Test
    public void testListFilePreservations() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFilePreservations(with(mockUser), with(KS_LABEL));
            will(returnValue(Set.of(fileList())));
        }});

        validateApiContract("/kickstart.profile.system/listFilePreservations", "GET")
                .withParams(Map.of("ksLabel", new String[]{KS_LABEL}))
                .onHandlerMethod("listFilePreservations", User.class, String.class);
    }

    @Test
    public void testAddFilePreservations() throws Exception {
        var filePreservations = List.of("test-file-list");

        context.checking(new Expectations() {{
            oneOf(handler()).addFilePreservations(with(mockUser), with(KS_LABEL), with(filePreservations));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/addFilePreservations", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "filePreservations", filePreservations))
                .onHandlerMethod("addFilePreservations", User.class, String.class, List.class);
    }

    @Test
    public void testRemoveFilePreservations() throws Exception {
        var filePreservations = List.of("test-file-list");

        context.checking(new Expectations() {{
            oneOf(handler()).removeFilePreservations(with(mockUser), with(KS_LABEL), with(filePreservations));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/removeFilePreservations", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "filePreservations", filePreservations))
                .onHandlerMethod("removeFilePreservations", User.class, String.class, List.class);
    }

    @Test
    public void testGetRegistrationType() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getRegistrationType(with(mockUser), with(KS_LABEL));
            will(returnValue("reactivation"));
        }});

        validateApiContract("/kickstart.profile.system/getRegistrationType", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL))
                .onHandlerMethod("getRegistrationType", User.class, String.class);
    }

    @Test
    public void testSetRegistrationType() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setRegistrationType(with(mockUser), with(KS_LABEL), with("reactivation"));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.system/setRegistrationType", "POST")
                .withBody(Map.of("ksLabel", KS_LABEL, "registrationType", "reactivation"))
                .onHandlerMethod("setRegistrationType", User.class, String.class, String.class);
    }
}
