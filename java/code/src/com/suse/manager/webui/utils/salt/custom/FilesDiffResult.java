/**
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;


import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.StateApplyResult;

import java.util.Map;
import java.util.Optional;

/**
 * Result to represent diffs of files(file, dir, symlink)
 *
 */
public class FilesDiffResult extends StateApplyResult<JsonElement> {

    protected JsonElement pchanges;

    /**
     * Return the pchanges
     * @return a map with all the changes
     */
    public Map getPChanges() {
        return JsonParser.GSON.fromJson(this.pchanges, Map.class);
    }

    /**
     * Return the pchanges
     * @param dataType Class type
     * @param <R> R type
     * @return Object dataType's class
     */
    public <R> R getPChanges(Class<R> dataType) {
        return JsonParser.GSON.fromJson(this.pchanges, dataType);
    }

    /**
     * Return the pchanges
     * @param dataType typeToken
     * @param <R> R type
     * @return Object of dataType.getType
     */
    public <R> R getPChanges(TypeToken<R> dataType) {
        return JsonParser.GSON.fromJson(this.pchanges, dataType.getType());
    }

    /**
     *FileResult object, to represent 'pchanges' contents for file
     */
    public static class FileResult {
        private Optional<String> newfile = Optional.empty();
        private Optional<String> diff =  Optional.empty();

        /**
         * @return the 'diff' attribute
         */
        public Optional<String> getDiff() {
            return diff;
        }

        /**
         * @return the 'newfile' attribute
         */
        public Optional<String> getNewfile() {
            return newfile;
        }
    }

    /**
     * SymLinkResult object, to represent 'pchanges' contents for symlink
     */
    public static class SymLinkResult {
        @SerializedName("new")
        private Optional<String> newSymlink =  Optional.empty();

        /**
         * @return the 'new' attribute
         */
        public Optional<String> getNewSymlink() {
            return newSymlink;
        }
    }

    /**
     * DirectoryResult object, to represent 'pchanges' contents for directory
     */
    public static class DirectoryResult {
        private  Optional<String> directory =  Optional.empty();
        private  Optional<String> user =  Optional.empty();
        private  Optional<String> mode =  Optional.empty();

        /**
         * @return the 'directory' attribute
         */
        public Optional<String> getDirectory() {
            return directory;
        }

        /**
         * @return the 'user' attribute
         */
        public Optional<String> getUser() {
            return user;
        }

        /**
         * @return the 'mode' attribute
         */
        public Optional<String> getMode() {
            return mode;
        }
    }
}
