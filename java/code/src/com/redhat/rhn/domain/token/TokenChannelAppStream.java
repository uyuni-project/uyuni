/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.domain.token;

import com.redhat.rhn.domain.channel.Channel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseRegTokenChannelAppStream")
public class TokenChannelAppStream {

    /**
     * Constructs a TokenChannelAppStream instance.
     */
    public TokenChannelAppStream() {
        // Default constructor
    }

    /**
     * Constructs a TokenChannelAppStream.
     *
     * @param tokenIn     the token
     * @param channelIn   the channel
     * @param appStreamIn the appStream in the format name:stream
     */
    public TokenChannelAppStream(Token tokenIn, Channel channelIn, String appStreamIn) {
        token = tokenIn;
        channel = channelIn;
        name = appStreamIn.split(":")[0];
        stream = appStreamIn.split(":")[1];
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_reg_tok_ch_as_id_seq")
    @SequenceGenerator(name = "suse_reg_tok_ch_as_id_seq", sequenceName = "suse_reg_tok_ch_as_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "token_id")
    private Token token;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String stream;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token tokenIn) {
        token = tokenIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String streamIn) {
        stream = streamIn;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    public String getAppStream() {
        return name + ":" + stream;
    }
}
