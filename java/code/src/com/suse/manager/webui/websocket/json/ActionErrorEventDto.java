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
package com.suse.manager.webui.websocket.json;

/**
 * Error message DTO.
 */
public class ActionErrorEventDto extends AbstractSaltEventDto {

    private String message;

    /**
     * @param minionIdIn the minion id
     * @param messageIn the error message
     */
    public ActionErrorEventDto(String minionIdIn, String messageIn) {
        super("error", minionIdIn);
        this.message = messageIn;
    }

    /**
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param messageIn the error message
     */
    public void setMessage(String messageIn) {
        this.message = messageIn;
    }
}
