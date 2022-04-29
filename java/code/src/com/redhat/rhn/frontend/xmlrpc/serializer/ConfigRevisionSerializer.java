/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.EncodedConfigRevision;
import com.redhat.rhn.frontend.xmlrpc.ConfigFileErrorException;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;


/**
 * ConfigRevisionSerializer
 *
 * @xmlrpc.doc
 * #struct_begin("configuration revision information")
 *   #prop("string", "type")
 *              #options()
 *                  #item("file")
 *                  #item("directory")
 *                  #item("symlink")
 *              #options_end()
 *   #prop_desc("string", "path","File Path")
 *   #prop_desc("string", "target_path","Symbolic link Target File Path.
 *                              Present for Symbolic links only.")
 *   #prop_desc("string", "channel","Channel Name")
 *   #prop_desc("string", "contents","File contents (base64 encoded according
                to the contents_enc64 attribute)")
 *   #prop_desc("boolean", "contents_enc64"," Identifies base64 encoded content")
 *   #prop_desc("int", "revision","File Revision")
 *   #prop_desc($date, "creation","Creation Date")
 *   #prop_desc($date, "modified","Last Modified Date")
 *   #prop_desc("string", "owner","File Owner. Present for files or directories only.")
 *   #prop_desc("string", "group","File Group. Present for files or directories only.")
 *   #prop_desc("int", "permissions","File Permissions (Deprecated).
 *                                  Present for files or directories only.")
 *   #prop_desc("string", "permissions_mode", "File Permissions.
 *                                      Present for files or directories only.")
 *   #prop_desc("string", "selinux_ctx", "SELinux Context (optional).")
 *   #prop_desc("boolean", "binary", "true/false , Present for files only.")
 *   #prop_desc("string", "sha256", "File's sha256 signature. Present for files only.")
 *   #prop_desc("string", "macro-start-delimiter",
 *          "Macro start delimiter for a config file. Present for text files only.")
 *   #prop_desc("string", "macro-end-delimiter",
 *          "Macro end delimiter for a config file. Present for text files only.")
 * #struct_end()
 */
public class ConfigRevisionSerializer extends ApiResponseSerializer<ConfigRevision> {

    public static final String CONTENTS = "contents";
    public static final String CONTENTS_ENC64 = "contents_enc64";
    public static final String PATH = "path";
    public static final String TARGET_PATH = "target_path";
    public static final String OWNER = "owner";
    public static final String GROUP = "group";
    public static final String SELINUX_CTX = "selinux_ctx";
    public static final String PERMISSIONS = "permissions";
    public static final String PERMISSIONS_MODE = "permissions_mode";
    public static final String MACRO_START = "macro-start-delimiter";
    public static final String MACRO_END = "macro-end-delimiter";
    public static final String BINARY = "binary";
    public static final String TYPE = "type";
    public static final String REVISION = "revision";

    @Override
    public Class<ConfigRevision> getSupportedClass() {
        return ConfigRevision.class;
    }

    @Override
    public SerializedApiResponse serialize(ConfigRevision src) {
        SerializationBuilder builder = new SerializationBuilder();

        if (src.getConfigFileType() != null) {
            builder.add(TYPE, src.getConfigFileType().getLabel());
        }

        builder.add(PATH, src.getConfigFile().getConfigFileName().getPath());
        builder.add(REVISION, src.getRevision());
        builder.add("creation", src.getCreated());
        builder.add("modified", src.getModified());
        builder.add(SELINUX_CTX, src.getConfigInfo().getSelinuxCtx());
        if (!src.isSymlink()) {
            builder.add(OWNER, src.getConfigInfo().getUsername());
            builder.add(GROUP, src.getConfigInfo().getGroupname());
            builder.add(PERMISSIONS, src.getConfigInfo().getFilemode());
            builder.add(PERMISSIONS_MODE, new DecimalFormat("000").format(
                src.getConfigInfo().getFilemode().longValue()));
        }
        else {
            builder.add(TARGET_PATH, src.getConfigInfo().getTargetFileName().getPath());
        }

        if (src.isFile() || src.isSls()) {
            builder.add(BINARY, src.getConfigContent().isBinary());
            builder.add("sha256", src.getConfigContent().getChecksum().getChecksum());
            if (src instanceof EncodedConfigRevision || src.getConfigContent().isBinary()) {
                addEncodedFileContent(src, builder);
            }
            else {
                addFileContent(src, builder);
            }

        }
        builder.add("channel", src.getConfigFile().getConfigChannel().getName());
        return builder.build();
    }

    protected void addFileContent(ConfigRevision rev, SerializationBuilder builder) {
        if (!rev.getConfigContent().isBinary()) {
            String content = rev.getConfigContent().getContentsString();
            if (!StringUtil.containsInvalidXmlChars2(content)) {
                builder.add(CONTENTS, content);
                builder.add(CONTENTS_ENC64, Boolean.FALSE);
            }
            else {
                throw new ConfigFileErrorException("The binary file was marked as text.");
            }
            builder.add(MACRO_START, rev.getConfigContent().getDelimStart());
            builder.add(MACRO_END, rev.getConfigContent().getDelimEnd());
        }
    }

    protected void addEncodedFileContent(ConfigRevision rev, SerializationBuilder builder) {
        builder.add(CONTENTS, new String(Base64.encodeBase64(
                rev.getConfigContent().getContents()), StandardCharsets.UTF_8));
        builder.add(CONTENTS_ENC64, Boolean.TRUE);
        if (!rev.getConfigContent().isBinary()) {
            builder.add(MACRO_START, rev.getConfigContent().getDelimStart());
            builder.add(MACRO_END, rev.getConfigContent().getDelimEnd());
        }
    }
}
