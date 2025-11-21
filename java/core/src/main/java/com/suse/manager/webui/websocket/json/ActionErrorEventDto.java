/*
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
    private String code;

    /**
     * @param minionIdIn the minion id
     * @param messageIn the error message
     * @param codeIn the code of the error
     */
    public ActionErrorEventDto(String minionIdIn, String codeIn, String messageIn) {
        super("error", minionIdIn);
        this.message = messageIn;
        this.code = codeIn;
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

    /**
     * @return the code of the error
     */
    public String getCode() {
        return code;
    }

    /**
     * @param codeIn the code of the error
     */
    public void setCode(String codeIn) {
        this.code = codeIn;
    }
}
