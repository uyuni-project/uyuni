/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.manager.model.hub;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.DatabaseEnumType;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.frontend.listview.PageControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

public class HubFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(HubFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Save a {@link IssHub} object
     * @param issServer object to save
     */
    public void save(IssServer issServer) {
        saveObject(issServer);
    }

    /**
     * Save a {@link IssPeripheralChannels} object
     * @param issPeripheralChannelsIn object to save
     */
    public void save(IssPeripheralChannels issPeripheralChannelsIn) {
        saveObject(issPeripheralChannelsIn);
    }

    /**
     * Delete the peripheral channels
     * @param issPeripheralChannelsIn the channels
     */
    public void deleteChannels(Set<IssPeripheralChannels> issPeripheralChannelsIn) {
        delete(issPeripheralChannelsIn, IssPeripheralChannels.class);
    }
    /**
     * Remove a {@ink IssPeripheral} object
     * @param peripheralIn the object to remove
     */
    public void remove(IssPeripheral peripheralIn) {
        removeObject(peripheralIn);
    }

    /**
     * Remove a {@ink IssHub} object
     * @param hubIn the object to remove
     */
    public void remove(IssHub hubIn) {
        removeObject(hubIn);
    }

    /**
     * Remove a {@link IssPeripheralChannels} object
     * @param issPeripheralChannelsIn object to remove
     */
    public void remove(IssPeripheralChannels issPeripheralChannelsIn) {
        removeObject(issPeripheralChannelsIn);
    }

    /**
     * Retrieves a {@link IssHub} by id
     * @param id the id of the hub
     * @return the hub object
     */
    public IssHub findHubById(long id) {
        return getSession().find(IssHub.class, id);
    }

    /**
     * Lookup {@link IssHub} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssHub} with the given FQDN or empty
     */
    public Optional<IssHub> lookupIssHubByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssHub WHERE fqdn = :fqdn", IssHub.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * Retrieves a {@link IssPeripheral} by id
     * @param id the id of the peripheral
     * @return the peripheral object
     */
    public IssPeripheral findPeripheralById(long id) {
        return getSession().find(IssPeripheral.class, id);
    }

    /**
     * Lookup {@link IssHub} object.
     * A peripheral server should have not more than 1 Hub
     * @return return {@link IssHub}
     */
    public Optional<IssHub> lookupIssHub() {
        Query<IssHub> query = getSession().createQuery("FROM IssHub", IssHub.class);
        if (query.stream().count() > 1) {
            LOG.error("Duplicate hub in IssHub: a peripheral server should have not more than 1 Hub");
        }
        return query.stream().findFirst();
    }

    /**
     * @return return true, when this system is an Inter-Server-Sync Peripheral Server
     */
    public boolean isISSPeripheral() {
        return lookupIssHub().isPresent();
    }

    /**
     * @return return true, when this system is an Inter-Server-Sync Hub Server
     */
    public boolean isISSHub() {
        return countPeripherals() != 0;
    }

    /**
     * get the list of all the peripheral servers for a hub
     *
     * @return a list of paginated peripherals
     */
    public List<IssPeripheral> listPeripherals() {
        return getSession().createQuery("FROM IssPeripheral", IssPeripheral.class).list();
    }

    /**
     * get number of peripheral registered on this server
     *
     * @return a number of peripherals
     */
    public long countPeripherals() {
        return getSession().createQuery("SELECT count(*) FROM IssPeripheral", Long.class).uniqueResult();
    }

    /**
     * get number of peripheral registered on this server
     *
     * @param pc the page control object
     * @return the number of peripherals
     */
    public long countPeripherals(PageControl pc) {
        if (pc == null) {
            return countPeripherals();
        }

        return buildCountQueryFromPageControl(IssPeripheral.class, pc).uniqueResult();
    }

    /**
     * get the list of all the peripheral servers for a hub
     * @param pc the page control object
     * @return a list of paginated peripherals
     */
    public List<IssPeripheral> listPaginatedPeripherals(PageControl pc) {
        if (pc == null) {
            return listPeripherals();
        }

        return buildListQueryFromPageControl(IssPeripheral.class, pc).list();
    }

    /**
     * Lookup {@link IssPeripheral} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssPeripheral} with the given FQDN or empty
     */
    public Optional<IssPeripheral> lookupIssPeripheralByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssPeripheral WHERE fqdn = :fqdn", IssPeripheral.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * List {@link IssPeripheralChannels} objects which reference
     * the given {@link IssPeripheral} server and {@link Channel}
     * @param peripheralIn the peripheral server
     * @param channelIn    the channel
     * @return return {@link IssPeripheralChannels} or empty
     */
    public Optional<IssPeripheralChannels> lookupIssPeripheralChannelsByFqdnAndChannel(IssPeripheral peripheralIn,
                                                                                       Channel channelIn) {
        return getSession()
                .createQuery("FROM IssPeripheralChannels WHERE peripheral = :peripheral AND channel = :channel",
                        IssPeripheralChannels.class)
                .setParameter("peripheral", peripheralIn)
                .setParameter("channel", channelIn)
                .uniqueResultOptional();
    }

    /**
     * List {@link IssPeripheralChannels} objects which reference the given {@link Channel}
     * @param channelIn the channel
     * @return return the list of {@link IssPeripheralChannels} objects
     */
    public List<IssPeripheralChannels> listIssPeripheralChannelsByChannels(Channel channelIn) {
        return getSession()
                .createQuery("FROM IssPeripheralChannels WHERE channel = :channel", IssPeripheralChannels.class)
                .setParameter("channel", channelIn)
                .list();
    }

    /**
     * List {@link IssPeripheralChannels} objects for the given {@link IssPeripheral} server
     * @param peripheralIn the peripheral server
     * @return return a list of all {@link IssPeripheralChannels} for the peripheral server
     */
    public List<IssPeripheralChannels> listIssPeripheralChannels(IssPeripheral peripheralIn) {
        return getSession()
                .createQuery("FROM IssPeripheralChannels WHERE peripheral = :peripheral", IssPeripheralChannels.class)
                .setParameter("peripheral", peripheralIn)
                .list();
    }

    /**
     * Store a new access token
     * @param fqdn the FQDN of the server
     * @param token the token to establish a connection with the specified server
     * @param type the type of token
     * @param expiration when the token is no longer valid
     * @return the id of the stored access token
     */
    public IssAccessToken saveToken(String fqdn, String token, TokenType type, Instant expiration) {
        // Lookup if this association already exists
        IssAccessToken accessToken = lookupAccessTokenByFqdnAndType(fqdn, type);
        if (accessToken == null) {
            accessToken = new IssAccessToken(type, token, fqdn, expiration);
        }
        else {
            accessToken.setToken(token);
            accessToken.setValid(true);
            accessToken.setExpirationDate(Date.from(expiration));
        }

        // Store the new token
        getSession().saveOrUpdate(accessToken);
        return accessToken;
    }

    /**
     * Updates an existing access token
     * @param accessToken the access token to update
     */
    public void updateToken(IssAccessToken accessToken) {
        getSession().merge(accessToken);
    }

    /**
     * Returns the issued access token information matching the given token
     * @param token the string representation of the token
     * @return the issued token, if present
     */
    public IssAccessToken lookupIssuedToken(String token) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.token = :token", IssAccessToken.class)
            .setParameter("type", TokenType.ISSUED)
            .setParameter("token", token)
            .uniqueResult();
    }

    /**
     * Returns the access token for the specified FQDN
     * @param fqdn the FQDN of the peripheral/hub
     * @return the access token associated to the entity, if present
     */
    public IssAccessToken lookupAccessTokenFor(String fqdn) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.serverFqdn = :fqdn", IssAccessToken.class)
            .setParameter("type", TokenType.CONSUMED)
            .setParameter("fqdn", fqdn)
            .uniqueResult();
    }

    /**
     * Returns the access token of the given type for the specified FQDN
     * @param fqdn the FQDN of the peripheral/hub
     * @param type the type of token
     * @return the access token associated to the entity, if present
     */
    public IssAccessToken lookupAccessTokenByFqdnAndType(String fqdn, TokenType type) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.serverFqdn = :fqdn", IssAccessToken.class)
            .setParameter("type", type)
            .setParameter("fqdn", fqdn)
            .uniqueResult();
    }

    /**
     * Retrieves the access token with the given id
     * @param id the id of the token
     * @return the access token instance, if present
     */
    public Optional<IssAccessToken> lookupAccessTokenById(long id) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.id = :id", IssAccessToken.class)
            .setParameter("id", id)
            .uniqueResultOptional();
    }

    /**
     * Returns a list of access tokens for specified FQDN
     * @param fqdn the FQDN of the server
     * @return return the access tokens associated with the given fqdn
     */
    public List<IssAccessToken> listAccessTokensByFqdn(String fqdn) {
        return getSession()
                .createQuery("FROM IssAccessToken k WHERE k.serverFqdn = :fqdn", IssAccessToken.class)
                .setParameter("fqdn", fqdn)
                .list();
    }

    /**
     * Delete all access tokens for the given server
     * @param serverFqdn the FQDN for the server
     * @return number of removed tokens
     */
    public int removeAccessTokensFor(String serverFqdn) {
        return getSession()
                .createNativeQuery("DELETE FROM suseISSAccessToken WHERE server_fqdn = :fqdn")
                .setParameter("fqdn", serverFqdn)
                .executeUpdate();
    }

    /**
     * Delete the access tokens with the given id
     * @param id the id of the token
     * @return true if the token was deleted, false otherwise
     */
    public boolean removeAccessTokenById(long id) {
        int tokenRemoved = getSession()
            .createNativeQuery("DELETE FROM suseISSAccessToken WHERE id = :id")
            .setParameter("id", id)
            .executeUpdate();

        return tokenRemoved != 0;
    }

    /**
     * Count the existing access tokens
     * @return the current number of access tokens
     */
    public long countAccessToken() {
        return getSession()
            .createQuery("SELECT COUNT(*) FROM IssAccessToken k", Long.class)
            .uniqueResult();
    }

    /**
     * Lists the existing access token
     * @param offset the first item to retrieve
     * @param pageSize the maximum number of items to retrieve
     * @return the list of tokens
     */
    public List<AccessTokenDTO> listAccessToken(int offset, int pageSize) {
        return getSession().createNativeQuery("""
                  SELECT k.id
                          , k.type
                          , k.server_fqdn
                          , k.valid
                          , k.expiration_date
                          , k.created
                          , k.modified
                          , h.id as hub_id
                          , p.id as peripheral_id
                    FROM suseissaccesstoken k
                          LEFT JOIN suseisshub h ON k.server_fqdn = h.fqdn
                          LEFT JOIN suseissperipheral p ON k.server_fqdn = p.fqdn
                ORDER BY k.created DESC
                """, Tuple.class)
            .addScalar("id", StandardBasicTypes.LONG)
            .addScalar("type", StandardBasicTypes.STRING)
            .addScalar("server_fqdn", StandardBasicTypes.STRING)
            .addScalar("valid", StandardBasicTypes.BOOLEAN)
            .addScalar("expiration_date", StandardBasicTypes.TIMESTAMP)
            .addScalar("created", StandardBasicTypes.TIMESTAMP)
            .addScalar("modified", StandardBasicTypes.TIMESTAMP)
            .addScalar("hub_id", StandardBasicTypes.LONG)
            .addScalar("peripheral_id", StandardBasicTypes.LONG)
            .setFirstResult(offset)
            .setMaxResults(pageSize)
            .stream()
            .map(tuple -> new AccessTokenDTO(
                tuple.get("id", Long.class),
                tuple.get("server_fqdn", String.class),
                DatabaseEnumType.findByLabel(TokenType.class, tuple.get("type", String.class)),
                tuple.get("valid", Boolean.class),
                tuple.get("expiration_date", Date.class),
                tuple.get("created", Date.class),
                tuple.get("modified", Date.class),
                tuple.get("hub_id", Long.class),
                tuple.get("peripheral_id", Long.class)
            ))
            .toList();
    }

    /**
     * Return list of {@link ChannelInfoDetailsJson} for a given {@link IssPeripheral} server
     * @param peripheral the peripheral server
     * @return return a list of ChannelInfoDetails for synchronization with the peripheral
     */
    public List<ChannelInfoDetailsJson> listChannelInfoForPeripheral(IssPeripheral peripheral) {
        List<IssPeripheralChannels> peripheralChannels = listIssPeripheralChannels(peripheral);
        Set<Long> channelIds = peripheralChannels.stream()
                .map(IssPeripheralChannels::getChannel)
                .map(Channel::getId)
                .collect(Collectors.toSet());
        return peripheralChannels.stream()
                .map(pc -> ChannelFactory.toChannelInfo(
                        pc.getChannel(),
                        pc.getPeripheralOrgId(),
                        findOriginalChannelLabel(pc.getChannel(), channelIds)))
                .toList();
    }

    /**
     * Traverses the chain of cloned channels to find the first "original" channel in the given set
     * @param channel the starting channel
     * @param channelIds the set of valid channel IDs
     * @return an Optional containing the original channel's label if found, or empty if not
     */
    private Optional<String> findOriginalChannelLabel(Channel channel, Set<Long> channelIds) {
        Channel search = channel;
        while (search.isCloned()) {
            search = search.asCloned().map(ClonedChannel::getOriginal).orElse(search);
            if (channelIds.contains(search.getId())) {
                return Optional.of(search.getLabel());
            }
        }
        return Optional.empty();
    }

    private static <E> Query<Long> buildCountQueryFromPageControl(Class<E> entityClass, PageControl pc) {
        Session session = getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<E> root = criteria.from(entityClass);

        criteria.select(builder.count(root));

        if (pc.hasFilter()) {
            criteria.where(builder.like(root.get(pc.getFilterColumn()), "%" + pc.getFilterData() + "%"));
        }

        return session.createQuery(criteria);
    }

    private static <E> Query<E> buildListQueryFromPageControl(Class<E> entityClass, PageControl pc) {
        Session session = getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<E> criteria = builder.createQuery(entityClass);

        Root<E> root = criteria.from(entityClass);

        criteria.select(root);

        if (pc.hasFilter()) {
            criteria.where(builder.like(root.get(pc.getFilterColumn()), "%" + pc.getFilterData() + "%"));
        }

        Path<Object> sortColumn = root.get(pc.getSortColumn());
        criteria.orderBy(pc.isSortDescending() ? builder.desc(sortColumn) : builder.asc(sortColumn));

        return session.createQuery(criteria)
            .setFirstResult(pc.getStart() - 1)
            .setMaxResults(pc.getPageSize());
    }
}
