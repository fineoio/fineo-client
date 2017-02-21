/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package io.fineo.read.auth.cognito;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import io.fineo.read.auth.cognito.handle.AuthenticationHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a user-pool in a Cognito identity provider account. The user-pools are called as
 * <b>Cognito User Identity Pool</b> or <b>User Identity Pool</b> or <b>User Pool</b>. All of these
 * terms represent the same entity, which is a pool of users in your account.
 * <p>
 *     A user-pool can have these:
 *
 *     1) User pool ID, {@code userPoolId}. This is an unique identifier for your user pool. This is
 *     a required parameter to use the SDK.
 *
 *     2) Client identifier, {@code clientId}. This is generated for a user pool and each user pool
 *     can have several of these. A client identifier will associated with one, and only one, user
 *     pool. This is required to use the SDK. A client identifier can have one or no client secrets.
 *
 *     3) Client secret, {@code clientSecret}. This is generated for a Client identified. A client
 *     identifier may have a client secret, it is not necessary to generate a client secret for all
 *     client identifiers. However if a client identifier has a client secret then this client secret
 *     has to be used, along with the client identifier, in the SDK.
 * </p>
 *
 * On a user-pool new user's can sign-up and create new {@link CognitoUser}.
 */
public class CognitoUserPool {
    /**
     * Cognito Your Identity Pool ID
     */
    private final String userPoolId;

    /**
     * Client ID created for your pool {@code userPoolId}.
     */
    private final String clientId;

    /**
     * Client secret generated for this {@code clientId}, this may be {@code null} if a secret is not
     * generated for the {@code clientId}.
     */
    private String clientSecret;

    /**
     * CIP low-level client.
     */
    private final AWSCognitoIdentityProviderClient client;

    /**
     * Calculated with {@code userId}, {@code clientId} and {@code clientSecret}
     */
    private String secretHash;

    private Map<String, String> tokenCache = new HashMap<>();

    /**
     * Constructs a user-pool with default {@link ClientConfiguration}.
     *
     * @param userPoolId            REQUIRED: User-pool-Id of the user-pool.
     * @param clientId              REQUIRED: Client-Id generated for this app and user-pool at the
     *                              Cognito Identity Provider developer console.
     * @param clientSecret          REQUIRED: Client Secret generated for this app and user-pool at
     *                              the Cognito Identity Provider developer console.
     */
    public CognitoUserPool(String userPoolId, String clientId, String clientSecret) {
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.client = new AWSCognitoIdentityProviderClient(new AnonymousAWSCredentials(), new
          ClientConfiguration());
    }

    /**
     * Returns Client ID set for this pool.
     *
     * @return Client ID.
     */
    public String getClientId() {
    	return  clientId;
    }

    /**
     * Returns Pool ID of this pool.
     *
     * @return Your User Pool ID.
     */
    public String getUserPoolId() {
        return userPoolId;
    }

    /**
     * Returns last authenticated user on this device in this user pool.
     *
     * @return An instance of the {@link CognitoUser} for last authenticated, cached on this device
     */
    public CognitoUser getCurrentUser() {
        String csiLastUserKey = "CognitoIdentityProvider." + clientId + ".LastAuthUser";

        if(tokenCache.containsKey(csiLastUserKey)){
            return getUser(tokenCache.getOrDefault(csiLastUserKey, null));
        }
        else {
            return getUser();
        }
    }

    /**
     * Returns a {@link CognitoUser} with no username set.
     *
     * @return {@link CognitoUser}.
     */
    public CognitoUser getUser() {
        return new CognitoUser(this, null, clientId, clientSecret, null, client);
    }

    /**
     * Returns a CognitoUser with userId {@code userId}
     * <p>
     *     This CognitoUser is not authenticated. Call {@link CognitoUser#getSession(AuthenticationHandler)}
     *     to get valid tokens {@link CognitoUserSession}
     * </p>
     *
     * @param userId            Can be null
     * @return a new CognitoUser instance with userId {@code userId}
     */
    public CognitoUser getUser(String userId) {
        if (userId == null) {
            return getUser();
        }

        if(userId.isEmpty()) {
            return getUser();
        }

        return new CognitoUser(this, userId, clientId, clientSecret,
                CognitoSecretHash.getSecretHash(userId, clientId, clientSecret),
                client);
    }
}
