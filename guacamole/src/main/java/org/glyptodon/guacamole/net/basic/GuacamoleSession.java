/*
 * Copyright (C) 2015 Glyptodon LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.glyptodon.guacamole.net.basic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.environment.Environment;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.auth.AuthenticatedUser;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains Guacamole-specific user information which is tied to the current
 * session, such as the UserContext and current clipboard state.
 *
 * @author Michael Jumper
 */
public class GuacamoleSession {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(GuacamoleSession.class);

    /**
     * The user associated with this session.
     */
    private AuthenticatedUser authenticatedUser;
    
    /**
     * All UserContexts associated with this session. Each
     * AuthenticationProvider may provide its own UserContext.
     */
    private List<UserContext> userContexts;

    /**
     * All currently-active tunnels, indexed by tunnel UUID.
     */
    private final Map<String, GuacamoleTunnel> tunnels = new ConcurrentHashMap<String, GuacamoleTunnel>();

    /**
     * The last time this session was accessed.
     */
    private long lastAccessedTime;
    
    /**
     * Creates a new Guacamole session associated with the given
     * AuthenticatedUser and UserContexts.
     *
     * @param environment
     *     The environment of the Guacamole server associated with this new
     *     session.
     *
     * @param authenticatedUser
     *     The authenticated user to associate this session with.
     *
     * @param userContexts
     *     The List of UserContexts to associate with this session.
     *
     * @throws GuacamoleException
     *     If an error prevents the session from being created.
     */
    public GuacamoleSession(Environment environment,
            AuthenticatedUser authenticatedUser,
            List<UserContext> userContexts)
            throws GuacamoleException {
        this.lastAccessedTime = System.currentTimeMillis();
        this.authenticatedUser = authenticatedUser;
        this.userContexts = userContexts;
    }

    /**
     * Returns the authenticated user associated with this session.
     *
     * @return
     *     The authenticated user associated with this session.
     */
    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Replaces the authenticated user associated with this session with the
     * given authenticated user.
     *
     * @param authenticatedUser
     *     The authenticated user to associated with this session.
     */
    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Returns a list of all UserContexts associated with this session. Each
     * AuthenticationProvider currently loaded by Guacamole may provide its own
     * UserContext for any successfully-authenticated user.
     *
     * @return
     *     An unmodifiable list of all UserContexts associated with this
     *     session.
     */
    public List<UserContext> getUserContexts() {
        return Collections.unmodifiableList(userContexts);
    }

    /**
     * Replaces all UserContexts associated with this session with the given
     * List of UserContexts.
     *
     * @param userContexts
     *     The List of UserContexts to associate with this session.
     */
    public void setUserContexts(List<UserContext> userContexts) {
        this.userContexts = userContexts;
    }
    
    /**
     * Returns whether this session has any associated active tunnels.
     *
     * @return true if this session has any associated active tunnels,
     *         false otherwise.
     */
    public boolean hasTunnels() {
        return !tunnels.isEmpty();
    }

    /**
     * Returns a map of all active tunnels associated with this session, where
     * each key is the String representation of the tunnel's UUID. Changes to
     * this map immediately affect the set of tunnels associated with this
     * session. A tunnel need not be present here to be used by the user
     * associated with this session, but tunnels not in this set will not
     * be taken into account when determining whether a session is in use.
     *
     * @return A map of all active tunnels associated with this session.
     */
    public Map<String, GuacamoleTunnel> getTunnels() {
        return tunnels;
    }

    /**
     * Associates the given tunnel with this session, such that it is taken
     * into account when determining session activity.
     *
     * @param tunnel The tunnel to associate with this session.
     */
    public void addTunnel(GuacamoleTunnel tunnel) {
        tunnels.put(tunnel.getUUID().toString(), tunnel);
    }

    /**
     * Disassociates the tunnel having the given UUID from this session.
     *
     * @param uuid The UUID of the tunnel to disassociate from this session.
     * @return true if the tunnel existed and was removed, false otherwise.
     */
    public boolean removeTunnel(String uuid) {
        return tunnels.remove(uuid) != null;
    }

    /**
     * Updates this session, marking it as accessed.
     */
    public void access() {
        lastAccessedTime = System.currentTimeMillis();
    }

    /**
     * Returns the time this session was last accessed, as the number of
     * milliseconds since midnight January 1, 1970 GMT. Session access must
     * be explicitly marked through calls to the access() function.
     *
     * @return The time this session was last accessed.
     */
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    /**
     * Closes all associated tunnels and prevents any further use of this
     * session.
     */
    public void invalidate() {

        // Close all associated tunnels, if possible
        for (GuacamoleTunnel tunnel : tunnels.values()) {
            try {
                tunnel.close();
            }
            catch (GuacamoleException e) {
                logger.debug("Unable to close tunnel.", e);
            }
        }

    }
    
}
