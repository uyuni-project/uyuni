/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenChannelAppStream;
import com.redhat.rhn.domain.token.TokenPackage;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* ActivationKeySerializer
*
* @apidoc.doc
*   #struct_begin("token serializer")
*     #prop("string", "description")
*     #prop("int", "usage_limit")
*     #prop("string", "base_channel_label")
*     #prop_array("child_channel_labels", "string", "child channel labels")
*     #prop_array("entitlements", "string", "entitlement labels")
*     #prop_array("server_group_ids", "string", "server group IDs")
*     #prop_array("package_names", "string", "package names")
*     #prop_array_begin("packages")
*       #struct_begin("package")
*         #prop_desc("name", "string", "package name")
*         #prop_desc("arch", "string", "arch label - optional")
*       #struct_end()
*     #prop_array_end()
*     #prop("boolean", "universal_default")
*     #prop("boolean", "disabled")
*   #struct_end()
*/
public class TokenSerializer extends ApiResponseSerializer<Token> {

   @Override
   public Class<Token> getSupportedClass() {
       return Token.class;
   }

    @Override
    public SerializedApiResponse serialize(Token src) {
        SerializationBuilder builder = new SerializationBuilder();
        populateTokenInfo(src, builder);
        return builder.build();
    }

   /**
    * Populates token information in to serializer format.
    * Since 95% of activation key serializer also uses this format
    *  it seemed prudent to make this a default access static method..
    * @param token the token to get the information to populate
    * @param builder the serialization builder that will be populated.
    */
   static void populateTokenInfo(Token token, SerializationBuilder builder) {
       // Locate the base channel, and store the others in a list of child channels:
       List<String> childChannelLabels = new LinkedList<>();
       String baseChannelLabel = null;
       for (Channel c : token.getChannels()) {
           if (c.isBaseChannel()) {
               baseChannelLabel = c.getLabel();
           }
           else {
               childChannelLabels.add(c.getLabel());
           }
       }
       if (baseChannelLabel == null) {
           baseChannelLabel = "none";
       }

       // Prepare a list of relevant entitlement labels, make sure to filter the
       // non-addon entitlements:
       List<String> entitlementLabels = new LinkedList<>();
       for (ServerGroupType sgt : token.getEntitlements()) {
           if (!sgt.isBase()) {
               entitlementLabels.add(sgt.getLabel());
           }
       }

       List<Integer> serverGroupIds = new LinkedList<>();
       for (ServerGroup group : token.getServerGroups()) {
           serverGroupIds.add(group.getId().intValue());
       }

       List<String> packageNames = new LinkedList<>();
       List<Map<String, String>> packages = new LinkedList<>();
       for (TokenPackage pkg : token.getPackages()) {
           packageNames.add(pkg.getPackageName().getName());

           Map<String, String> pkgMap = new HashMap<>();
           pkgMap.put("name", pkg.getPackageName().getName());

           if (pkg.getPackageArch() != null) {
               pkgMap.put("arch", pkg.getPackageArch().getLabel());
           }
           packages.add(pkgMap);
       }

       // Channel label is the key and a List of appStreams (name:stream) for each channel.
       Map<String, List<String>> appStreams = token.getAppStreams().stream()
           .collect(Collectors.groupingBy(
               tokenChannelAppStream -> tokenChannelAppStream.getChannel().getLabel(),
               Collectors.mapping(TokenChannelAppStream::getAppStream, Collectors.toList())
           ));

       builder.add("description", token.getNote());

       int usageLimit = 0;
       if (token.getUsageLimit() != null) {
           usageLimit = token.getUsageLimit().intValue();
       }
       builder.add("usage_limit", usageLimit);

       builder.add("base_channel_label", baseChannelLabel);
       builder.add("child_channel_labels", childChannelLabels);
       builder.add("entitlements", entitlementLabels);
       builder.add("server_group_ids", serverGroupIds);
       builder.add("package_names", packageNames);
       builder.add("packages", packages);
       builder.add("app_streams", appStreams);

       Boolean universalDefault = token.isOrgDefault();
       builder.add("universal_default", universalDefault);
       builder.add("disabled", token.isTokenDisabled());

       // Return the contact method label (e.g. 'ssh-push')
       builder.add("contact_method", token.getContactMethod().getLabel());
   }
}
