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

package io.fineo.client.auth.cognito;

import io.fineo.client.auth.cognito.exception.CognitoInternalErrorException;
import io.fineo.client.auth.cognito.exception.CognitoNotAuthorizedException;
import io.fineo.client.auth.cognito.exception.CognitoParameterInvalidException;
import io.fineo.client.auth.cognito.handle.AuthenticationContinuation;
import io.fineo.client.auth.cognito.handle.AuthenticationDetails;
import io.fineo.client.auth.cognito.handle.AuthenticationHandler;
import io.fineo.client.auth.cognito.handle.GenericHandler;
import io.fineo.client.auth.cognito.token.CognitoAccessToken;
import io.fineo.client.auth.cognito.token.CognitoDeviceHelper;
import io.fineo.client.auth.cognito.token.CognitoIdToken;
import io.fineo.client.auth.cognito.token.CognitoRefreshToken;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ConfirmDeviceRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmDeviceResult;
import com.amazonaws.services.cognitoidp.model.DeviceSecretVerifierConfigType;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.NewDeviceMetadataType;
import com.amazonaws.services.cognitoidp.model.ResourceNotFoundException;
import com.amazonaws.services.cognitoidp.model.RespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.RespondToAuthChallengeResult;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.concurrent.RecursiveTask;

/**
 * Represents a single Cognito User.
 * <p>
 *     This class encapsulates all operations possible on a user and all tokens belonging to the user.
 *     The user tokens, as {@link CognitoUserSession}, are stored in SharedPreferences. Only the tokens
 *     belonging to the last successfully authenticated user are stored.
 * </p>
 */
public class CognitoUser {

    private static final Logger LOG = LoggerFactory.getLogger(CognitoUser.class);

    private final String TAG = "CognitoUser: {}";

    /**
     * CIP low-level client.
     */
    private final AWSCognitoIdentityProvider cognitoIdentityProviderClient;

    /**
     * Client ID for Your Identity Pool.
     */
    private final String clientId;

    /**
     * Client secret generated for this {@code clientId}, this may be {@code null} if a secret is not
     * generated for the {@code clientId}.
     */
    private final String clientSecret;

    /**
     * userId for this user, this is mutable to allow the userId to be set during authentication.
     * This can be the username (users' unique sign-in username) or an alias (if available, such as email or phone number).
     */
    private String userId;

    /**
     * Username used for authentication process. This will be set from the results in the pre-io.fineo.client.auth.auth API call.
     */
    private String usernameInternal;

    /**
     * Device-key of this device, if available.
     */
    private String deviceKey;

    /**
     * Reference to the {@link CognitoUserPool} to which this user belongs .
     */
    private CognitoUserPool pool;

    /**
     * Secret-Hash for this user-pool, this is mutable because userId is mutable.
     */
    private String secretHash;

    /**
     * The current session.
     */
    private CognitoUserSession cipSession;

    private Map<String, String> tokenCache = new HashMap<>();
    private final CognitoDeviceHelper devices;

    /**
     * Constructs a new Cognito User from a Cognito user identity pool {@link CognitoUserPool} and userId.
     *
     * @param pool              REQUIRED: Reference to {@link CognitoUserPool}, to which this user belongs.
     * @param userId            REQUIRED: userId of this user.
     * @param clientId			REQUIRED: Client-Id of the android app.
     * @param clientSecret      REQUIRED: Client secret assigned for this Client-Id.
     * @param secretHash		REQUIRED: Secret-Hash, calculated for this android app.
     * @param client			REQUIRED: Low level client.
     */
    protected CognitoUser(CognitoUserPool pool, String userId,
                          String clientId, String clientSecret, String secretHash,
      AWSCognitoIdentityProvider client) {
        this.pool = pool;
        this.userId = userId;
        this.cognitoIdentityProviderClient = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.secretHash = secretHash;
        this.deviceKey = null;
        cipSession = null;
        this.devices = new CognitoDeviceHelper(tokenCache);
    }

    /**
     * Returns the userId of this user.
     *
     * @return userId.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the pool Id of this user.
     *
     * @return pool Id.
     */
    public String getUserPoolId() {
        return pool.getUserPoolId();
    }

    /**
     * Method low-level client for Amazon Cognito Identity Provider.
     *
     * @return
     */
    protected AWSCognitoIdentityProvider getCognitoIdentityProviderClient() {
        return cognitoIdentityProviderClient;
    }

    /**
     * Returns a valid tokens for a user through the callback method. Runs in background.
     * {@link AuthenticationHandler#onSuccess(CognitoUserSession, Object)}.
     * <p>
     *     Tokens are passed as instance of {@link CognitoUserSession}.
     *     Call this method to get valid tokens for a user. This method returns any valid cached
     *     tokens for the user. If no valid cached tokens are available this method initiates the
     *     process to authenticate the user and get tokens from Cognito Identity Provider service.
     *     Implement the interface {@link AuthenticationHandler} and pass it as callback to this
     *     method. This method uses the callback to interact with application at different
     *     stages of the authentication process. Continuation objects are used when the authentication
     *     process requires more data to continue.
     *     <b>Note:</b> This method will perform network operations. Calling this method in
     *     applications' main thread will cause Android to throw NetworkOnMainThreadException.
     * </p>
     * @param callback      REQUIRED: {@link AuthenticationHandler} callback
     */
    public void getSession(final AuthenticationHandler callback) {
        if (callback == null) {
            throw new InvalidParameterException("callback is null");
        }

        try {
            getCachedSession();
            callback.onSuccess(cipSession, null);
        } catch (InvalidParameterException e) {
            callback.onFailure(e);
        } catch (CognitoNotAuthorizedException e) {
           AuthenticationContinuation authenticationContinuation =
                   new AuthenticationContinuation(this, AuthenticationContinuation.RUN_IN_CURRENT, callback);
            callback.getAuthenticationDetails(authenticationContinuation, getUserId());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    /**
     * Initiates user authentication through the generic io.fineo.client.auth.auth flow (also called as Enhanced or Custom authentication).
     * This is the first step in user authentication. The response to this step from the service will contain
     * information about the next step in the authentication process.
     *
     * @param authenticationDetails         REQUIRED: Contains details about the user authentication.
     * @param callback                      REQUIRED: {@link AuthenticationHandler} callback.
     * @return {@link Runnable} for the next step in user authentication.
     */
    public Runnable initiateUserAuthentication(final AuthenticationDetails authenticationDetails, final AuthenticationHandler callback, final boolean runInBackground) {
        if (AuthenticationDetails.PASSWORD_AUTHENTICATION.equals(authenticationDetails.getAuthenticationType())) {
            return startWithUserSrpAuth(authenticationDetails, callback, runInBackground);
        } else if (AuthenticationDetails.CUSTOM_AUTHENTICATION.equals(authenticationDetails.getAuthenticationType())) {
            throw new UnsupportedOperationException(AuthenticationDetails.CUSTOM_AUTHENTICATION
                                                    + " not supported in forked Cognito pool");
        } else {
            return new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(new CognitoParameterInvalidException("Unsupported authentication type " + authenticationDetails.getAuthenticationType()));
                }
            };
        }
    }

    /**
     * Responds to an MFA challenge. This method creates a response to the challenge and calls the
     * internal method to respond to the authentication challenge.
     *
     * @param mfaCode                   REQUIRED: The MFA code received by the user.
     * @param challenge                 REQUIRED: Current challenge {@link RespondToAuthChallengeResult}.
     * @param callback                  REQUIRED: {@link AuthenticationHandler} callback.
     * @return {@link Runnable} for the next step in user authentication.
     */
    public Runnable respondToMfaChallenge(final String mfaCode, final RespondToAuthChallengeResult challenge, final AuthenticationHandler callback, final boolean runInBackground) {
        final RespondToAuthChallengeRequest challengeResponse = new RespondToAuthChallengeRequest();
        Map<String, String> mfaParameters = new HashMap<String, String>();
        mfaParameters.put("SMS_MFA_CODE", mfaCode);
        mfaParameters.put("USERNAME", usernameInternal);
        mfaParameters.put("DEVICE_KEY", deviceKey);
        mfaParameters.put("SECRET_HASH", secretHash);
        challengeResponse.setClientId(clientId);
        challengeResponse.setSession(challenge.getSession());
        challengeResponse.setChallengeName(challenge.getChallengeName());
        challengeResponse.setChallengeResponses(mfaParameters);
        return respondToChallenge(challengeResponse, callback, runInBackground);
    }

    /**
     * This method sends the challenge response to the Cognito IDP service. The call to the Cognito IDP
     * service returns a new challenge and a different method is called to process the challenge.
     * Restarts authentication if the service cannot find a device-key.
     *
     * @param challengeResponse             REQUIRED: {@link RespondToAuthChallengeRequest} contains
     *                                      response for the current challenge.
     * @param callback                      REQUIRED: {@link AuthenticationHandler} callback.
     * @param runInBackground               REQUIRED: Boolean to indicate the current threading.
     * @return {@link Runnable} for the next step in user authentication.
     */
    public Runnable respondToChallenge(final RespondToAuthChallengeRequest challengeResponse, final AuthenticationHandler callback, final boolean runInBackground) {
        try {
            if (challengeResponse != null && challengeResponse.getChallengeResponses() != null) {
                Map<String, String> challengeResponses = challengeResponse.getChallengeResponses();
                challengeResponses.put("DEVICE_KEY", deviceKey);
                challengeResponse.setChallengeResponses(challengeResponses);
            }
            RespondToAuthChallengeResult challenge = cognitoIdentityProviderClient.respondToAuthChallenge(challengeResponse);
            return handleChallenge(challenge, callback, runInBackground);
        } catch (final ResourceNotFoundException rna) {
            final CognitoUser cognitoUser = this;
            if (rna.getMessage().contains("Device")) {
              return clearCache(cognitoUser, runInBackground, callback);
            } else {
                return new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(rna);
                    }
                };
            }
        } catch (final Exception e) {
            return new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(e);
                }
            };
        }
    }

    private Runnable clearCache(final CognitoUser cognitoUser, final boolean runInBackground, final
    AuthenticationHandler callback){
      throw new IllegalStateException("No support for handling device "
                                      + "authentication/validation. Cannot remove a "
                                      + "cached device as we 'should' here.");
//                CognitoDeviceHelper.clearCachedDevice(usernameInternal, pool.getUserPoolId(), context);
//                return new Runnable() {
//                    @Override
//                    public void run() {
//                        AuthenticationContinuation authenticationContinuation =
//                          new AuthenticationContinuation(cognitoUser, context, runInBackground, callback);
//                        callback.getAuthenticationDetails(authenticationContinuation, cognitoUser.getUserId());
//                    }
//                };
    }


  /**
     * Call this method for valid, cached tokens for this user.
     *
     * @return Valid, cached tokens {@link CognitoUserSession}. {@code null} otherwise.
     */
    protected CognitoUserSession getCachedSession() {
        if (userId == null) {
            throw new CognitoNotAuthorizedException("User-ID is null");
        }

        if (cipSession != null) {
            if (cipSession.isValid()) {
                return cipSession;
            }
        }

        CognitoUserSession cachedTokens = readCachedTokens();

        if (cachedTokens.isValid()) {
            cipSession = cachedTokens;
            return  cipSession;
        }

        if (cachedTokens.getRefreshToken() != null) {
            try {
                cipSession = refreshSession(cachedTokens);
                cacheTokens(cipSession);
                return cipSession;
            } catch (Exception e) {
                clearCachedTokens();
                throw new CognitoNotAuthorizedException("user is not authenticated");
            }
        }
        throw new CognitoNotAuthorizedException("user is not authenticated");
    }

    /**
     * Sign-Out this user by removing all cached tokens.
     */
    public void signOut() {
        cipSession = null;
        clearCachedTokens();
    }

    /**
     * Sign-out from all devices associated with this user, in background.
     *
     * @param callback          REQUIRED: {@link GenericHandler} callback.
     */
    public void globalSignOutInBackground(final GenericHandler callback) {

        if (callback == null) {
            throw new CognitoParameterInvalidException("callback is null");
        }
        final CognitoUser user = this;

        new RecursiveTask(){

            @Override
            protected Object compute() {
                final Runnable returnCallback;
                try {
                    CognitoUserSession session = user.getCachedSession();
                    globalSignOutInternal(session);
                    new RecursiveTask() {
                        @Override
                        protected Object compute() {
                            signOut();
                            callback.onSuccess();
                            return null;
                        }
                    }.fork().join();
                } catch (final Exception e) {
                    new RecursiveTask(){
                        @Override
                        protected Object compute() {
                            callback.onFailure(e);
                            return null;
                        }
                    }.fork().join();
                }
                return null;
            }
        }.fork().join();
    }

    /**
     * Sign-out from all devices associated with this user, in current thread.
     *
     * <p>
     *     <b>Note:</b> This method will perform network operations. Calling this method in
     *     applications' main thread will cause Android to throw NetworkOnMainThreadException.
     * </p>
     * @param callback          REQUIRED: {@link GenericHandler} callback.
     */
    public void globalSignOut (GenericHandler callback) {
        if (callback == null) {
            throw new CognitoParameterInvalidException("callback is null");
        }

        try {
            globalSignOutInternal(this.getCachedSession());
            signOut();
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    /**
     * Internal method to Sign-Out from all devices of this user.
     *
     * @param session                  REQUIRED: {@link GenericHandler} callback.
     */
    private void globalSignOutInternal(CognitoUserSession session) {
        // Check if session is valid
        if (session == null) {
            throw new CognitoNotAuthorizedException("user is not authenticated");
        }

        if (!session.isValid()) {
            throw new CognitoNotAuthorizedException("user is not authenticated");
        }

        GlobalSignOutRequest globalSignOutRequest = new GlobalSignOutRequest();
        globalSignOutRequest.setAccessToken(getCachedSession().getAccessToken().getJWTToken());

        cognitoIdentityProviderClient.globalSignOut(globalSignOutRequest);
    }

    /**
     * Removes all cached tokens.
     */
    private void clearCachedTokens() {
        try {
            // Clear all cached tokens and last logged in user.
           tokenCache.clear();
        } catch (Exception e) {
            // Logging exception, this is not a fatal error
            LOG.error(TAG, "Error while deleting from SharedPreferences");
        }
    }

    /**
     * Checks for any valid tokens.
     *
     * @return {@link CognitoUserSession} if cached tokens are available.
     */
    private CognitoUserSession readCachedTokens() {
        CognitoUserSession userSession = new CognitoUserSession(null, null, null);

        try {
            // Format "key" strings
            String csiIdTokenKey        = "CognitoIdentityProvider." + clientId + "." + userId + ".idToken";
            String csiAccessTokenKey    = "CognitoIdentityProvider." + clientId + "." + userId + ".accessToken";
            String csiRefreshTokenKey   = "CognitoIdentityProvider." + clientId + "." + userId + ".refreshToken";

            if (tokenCache.containsKey(csiIdTokenKey)) {
                CognitoIdToken
                  csiCachedIdToken = new CognitoIdToken(tokenCache.getOrDefault(csiIdTokenKey, null));
                CognitoAccessToken csiCachedAccessToken = new CognitoAccessToken(tokenCache.getOrDefault(csiAccessTokenKey, null));
                CognitoRefreshToken csiCachedRefreshToken = new CognitoRefreshToken(tokenCache.getOrDefault(csiRefreshTokenKey, null));
                userSession = new CognitoUserSession(csiCachedIdToken, csiCachedAccessToken, csiCachedRefreshToken);
            }
        } catch (Exception e) {
            // Logging exception, this is not a fatal error
            LOG.error(TAG, "Error while reading SharedPreferences");
        }
        return  userSession;
    }

    /**
     * Cache tokens locally.
     *
     * @param session           REQUIRED: Tokens to be cached.
     */
    private void cacheTokens(CognitoUserSession session) {
        try {
            String csiUserPoolId = pool.getUserPoolId();

            // Create keys to look for cached tokens
            String csiIdTokenKey        = "CognitoIdentityProvider." + clientId + "." + userId + ".idToken";
            String csiAccessTokenKey    = "CognitoIdentityProvider." + clientId + "." + userId + ".accessToken";
            String csiRefreshTokenKey   = "CognitoIdentityProvider." + clientId + "." + userId + ".refreshToken";
            String csiLastUserKey       = "CognitoIdentityProvider." + clientId + ".LastAuthUser";

            // Store the data in Shared Preferences
            tokenCache.put(csiIdTokenKey, session.getIdToken().getJWTToken());
            tokenCache.put(csiAccessTokenKey, session.getAccessToken().getJWTToken());
            tokenCache.put(csiRefreshTokenKey, session.getRefreshToken().getToken());
            tokenCache.put(csiLastUserKey, userId);

        } catch (Exception e) {
            // Logging exception, this is not a fatal error
            LOG.error(TAG, "Error while writing to SharedPreferences.", e);
        }
    }

    /**
     * Creates a user session with the tokens from authentication.
     *
     * @param authResult                        REQUIRED: Authentication result which contains the
     *                                          tokens.
     * @return {@link CognitoUserSession} with the latest tokens.
     */
    private CognitoUserSession getCognitoUserSession(AuthenticationResultType authResult) {
        return getCognitoUserSession(authResult, null);
    }

    /**
     * Creates a user session with the tokens from authentication and overrider the refresh token
     * with the value passed.
     *
     * @param authResult                        REQUIRED: Authentication result which contains the
     *                                          tokens.
     * @param refreshTokenOverride              REQUIRED: This will be used to create a new session
     *                                          object if it is not null.
     * @return {@link CognitoUserSession} with the latest tokens.
     */
    private CognitoUserSession getCognitoUserSession(AuthenticationResultType authResult,
                                                     CognitoRefreshToken refreshTokenOverride) {
        String idtoken = authResult.getIdToken();
        CognitoIdToken idToken = new CognitoIdToken(idtoken);

        String acctoken = authResult.getAccessToken();
        CognitoAccessToken accessToken = new CognitoAccessToken(acctoken);

        CognitoRefreshToken refreshToken;

        if (refreshTokenOverride != null) {
            refreshToken = refreshTokenOverride;
        } else {
            String reftoken = authResult.getRefreshToken();
            refreshToken = new CognitoRefreshToken(reftoken);
        }
        return new CognitoUserSession(idToken, accessToken, refreshToken);
    }

    /**
     * Internal method to refresh current {@link CognitoUserSession}, is a refresh token is available.
     *
     * @param currSession           REQUIRED: Current cached {@link CognitoUserSession}.
     * @return {@link CognitoUserSession} with new access and id tokens.
     */
    private CognitoUserSession refreshSession(CognitoUserSession currSession) {
        CognitoUserSession cognitoUserSession = null;
        InitiateAuthRequest initiateAuthRequest = initiateRefreshTokenAuthRequest(currSession);
        InitiateAuthResult refreshSessionResult = cognitoIdentityProviderClient.initiateAuth(initiateAuthRequest);
        if (refreshSessionResult.getAuthenticationResult() == null) {
            throw new CognitoNotAuthorizedException("user is not authenticated");
        }
        cognitoUserSession = getCognitoUserSession(refreshSessionResult.getAuthenticationResult(), currSession.getRefreshToken());
        return cognitoUserSession;
    }

    /**
     * This method starts the user authentication with user password verification.
     * Restarts authentication if the service cannot find a device-key.
     *
     * @param authenticationDetails         REQUIRED: {@link AuthenticationDetails} contains user details
     *                                      for authentication.
     * @param callback                      REQUIRED: {@link AuthenticationHandler} callback.
     * @param runInBackground               REQUIRED: Boolean to indicate the current threading.
     * @return {@link Runnable} for the next step in user authentication.
     */
    private Runnable startWithUserSrpAuth(final AuthenticationDetails authenticationDetails, final AuthenticationHandler callback, final boolean runInBackground) {
        AuthenticationHelper authenticationHelper = new AuthenticationHelper(pool.getUserPoolId());
        InitiateAuthRequest initiateAuthRequest = initiateUserSrpAuthRequest(authenticationDetails, authenticationHelper);
        try {
            InitiateAuthResult initiateAuthResult = cognitoIdentityProviderClient.initiateAuth(initiateAuthRequest);
            updateInternalUsername(initiateAuthResult.getChallengeParameters());
            // verify that the password matches
            if (initiateAuthResult.getChallengeName().equals("PASSWORD_VERIFIER")) {
                if (authenticationDetails.getPassword() != null) {
                    RespondToAuthChallengeRequest challengeRequest = userSrpAuthRequest(initiateAuthResult, authenticationDetails, authenticationHelper);
                    return respondToChallenge(challengeRequest, callback, runInBackground);
                }
            }
            return handleChallenge(initiateAuthResult, callback, runInBackground);
        } catch (final ResourceNotFoundException rna) {
            final CognitoUser cognitoUser = this;
            if (rna.getMessage().contains("Device")) {
              return clearCache(cognitoUser, runInBackground, callback);
            } else {
                return new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(rna);
                    }
                };
            }
        } catch (final Exception e) {
            return new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(e);
                }
            };
        }
    }

    /**
     * Creates response for the second step of the SRP authentication.
     *
     * @param challenge                     REQUIRED: {@link InitiateAuthResult} contains next challenge.
     * @param authenticationDetails         REQUIRED: {@link AuthenticationDetails} user authentication details.
     * @param authenticationHelper          REQUIRED: Internal helper class for SRP calculations.
     * @return {@link RespondToAuthChallengeRequest}.
     */
    private RespondToAuthChallengeRequest userSrpAuthRequest(InitiateAuthResult challenge,
      AuthenticationDetails authenticationDetails,
      AuthenticationHelper authenticationHelper) {
        this.usernameInternal = challenge.getChallengeParameters().get("USERNAME");
        this.deviceKey = devices.getDeviceKey(usernameInternal, getUserPoolId());
        secretHash = CognitoSecretHash.getSecretHash(usernameInternal, clientId, clientSecret);

        BigInteger B = new BigInteger(challenge.getChallengeParameters().get("SRP_B"), 16);
        if (B.mod(AuthenticationHelper.N).equals(BigInteger.ZERO)) {
            throw new CognitoInternalErrorException("SRP error, B cannot be zero");
        }

        BigInteger salt = new BigInteger(challenge.getChallengeParameters().get("SALT"), 16);
        byte[] key = authenticationHelper.getPasswordAuthenticationKey(usernameInternal, authenticationDetails.getPassword(), B, salt);

        Date timestamp = new Date();
        byte[] hmac;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            mac.update(pool.getUserPoolId().split("_", 2)[1].getBytes(StringUtils.UTF8));
            mac.update(usernameInternal.getBytes(StringUtils.UTF8));
            byte[] secretBlock = Base64.decode(challenge.getChallengeParameters().get("SECRET_BLOCK"));
            mac.update(secretBlock);
            SimpleDateFormat
              simpleDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
            simpleDateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            String dateString = simpleDateFormat.format(timestamp);
            byte[] dateBytes = dateString.getBytes(StringUtils.UTF8);
            hmac = mac.doFinal(dateBytes);
        } catch (Exception e) {
            throw new CognitoInternalErrorException("SRP error", e);
        }

        SimpleDateFormat formatTimestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
        formatTimestamp.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

        Map<String, String> srpAuthResponses = new HashMap<String, String>();
        srpAuthResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", challenge.getChallengeParameters().get("SECRET_BLOCK"));
        srpAuthResponses.put("PASSWORD_CLAIM_SIGNATURE", new String(Base64.encode(hmac), StandardCharsets.UTF_8));
        srpAuthResponses.put("TIMESTAMP", formatTimestamp.format(timestamp));
        srpAuthResponses.put("USERNAME", usernameInternal);
        srpAuthResponses.put("USER_ID_FOR_SRP", usernameInternal);
        srpAuthResponses.put("DEVICE_KEY", deviceKey);
        srpAuthResponses.put("SECRET_HASH", secretHash);

        RespondToAuthChallengeRequest authChallengeRequest = new RespondToAuthChallengeRequest();
        authChallengeRequest.setChallengeName(challenge.getChallengeName());
        authChallengeRequest.setClientId(clientId);
        authChallengeRequest.setSession(challenge.getSession());
        authChallengeRequest.setChallengeResponses(srpAuthResponses);

        return authChallengeRequest;
    }


    /**
     * Find the next step from the challenge.
     * This is an important step in the generic authentication flow. After the responding to a challenge,
     * the results are analyzed here to determine the next step in the authentication process.
     * Like all other methods in this SDK, this is designed to work with Continuation objects.
     * This method returns a {@link Runnable} with the code to be executed, for the next step, to the invoking Continuation.
     * The possible steps are
     *  1) Authentication was successful and we have the tokens, in this case we call {@code onSuccess()} to return the tokens.
     *  2) User password is required, an AuthenticationContinuation is created.
     *  3) MFA validation is required, a MultiFactorAuthenticationContinuation object is created.
     *  4) Other generic challenge, the challenge details are passed to the user.
     *
     * @param challenge                 REQUIRED: Current challenge details, {@link RespondToAuthChallengeResult}.
     * @param callback                  REQUIRED: {@link AuthenticationDetails} callback.
     * @param runInBackground           REQUIRED: Boolean to indicate the current threading.
     * @return {@link Runnable} for the next step in user authentication.
     */
    private Runnable handleChallenge(final RespondToAuthChallengeResult challenge, final AuthenticationHandler callback, final boolean runInBackground) {
        Runnable nextTask;
        final CognitoUser cognitoUser = this;
        nextTask = new Runnable() {
            @Override
            public void run() {
                callback.onFailure(new CognitoInternalErrorException("Authentication failed due to an internal error"));
            }
        };

        if (challenge == null) {
            return  nextTask;
        }

        updateInternalUsername(challenge.getChallengeParameters());
        String challengeName = challenge.getChallengeName();
        if (challengeName == null) {
            LOG.debug("Got challenge: {}", challenge);
            final CognitoUserSession cognitoUserSession = getCognitoUserSession(challenge.getAuthenticationResult());
            cacheTokens(cognitoUserSession);
            NewDeviceMetadataType newDeviceMetadata = challenge.getAuthenticationResult().getNewDeviceMetadata();
            if (newDeviceMetadata == null) {
                nextTask = new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(cognitoUserSession, null);
                    }
                };
            } else {
                ConfirmDeviceResult confirmDeviceResult = confirmDevice(newDeviceMetadata);
                if (confirmDeviceResult != null && confirmDeviceResult.isUserConfirmationNecessary()) {
                    final CognitoDevice newDevice = new CognitoDevice(newDeviceMetadata
                      .getDeviceKey(), null, null, null, null, cognitoUser);
                    nextTask = new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(cognitoUserSession, newDevice);
                        }
                    };
                } else {
                    nextTask = new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(cognitoUserSession, null);
                        }
                    };
                }
            }
        } else if (challengeName.equals("PASSWORD_VERIFIER")) {
            return nextTask;
        } else if (challengeName.equals("SMS_MFA")) {
          throw new UnsupportedOperationException("MFA challenges not supported!");
        } else if (challengeName.equals("DEVICE_SRP_AUTH")) {
            nextTask = deviceSrpAuthentication(challenge, callback, runInBackground);
        } else {
          throw new RuntimeException("Generic challenge continuation no supported!");
//            final ChallengeContinuation challengeContinuation =
//                    new ChallengeContinuation(cognitoUser, context, clientId, challenge, runInBackground, callback);
//            nextTask = new Runnable() {
//                @Override
//                public void run() {
//                    callback.authenticationChallenge(challengeContinuation);
//                }
//            };
        }
        return nextTask;
    }

    /**
     * The method confirms a device. If this device can be remembered and if this is a new device,
     * a new device key is generated at the end of a successful authentication. SRP verification is
     * performed by the service, during the next authentication attempts, to identify this device.
     * This method generates the necessary tokens to enable the device SRP verification.
     *
     * @param deviceMetadata        REQUIRED: Metadata for the new device.
     */
    private ConfirmDeviceResult confirmDevice(final NewDeviceMetadataType deviceMetadata) {
        Map<String, String> deviceSrpVerifiers = CognitoDeviceHelper.generateVerificationParameters(deviceKey, deviceMetadata.getDeviceGroupKey());

        ConfirmDeviceResult confirmDeviceResult = new ConfirmDeviceResult();
        confirmDeviceResult.setUserConfirmationNecessary(false);
        try {
            confirmDeviceResult = confirmDeviceInternal(getCachedSession(), deviceMetadata
              .getDeviceKey(), deviceSrpVerifiers.get("verifier"), deviceSrpVerifiers.get("salt"), "Fineo.JDBC");
        } catch (Exception e) {
            LOG.error(TAG, "Device confirmation failed: " + e.getMessage());
            return null;
        }
        this.devices.cacheDevice(usernameInternal,getUserPoolId(), deviceMetadata);
        return confirmDeviceResult;
    }

    /**
     * Internal method to confirm a device.
     *
     * @param session                           REQUIRED: A valid {@link CognitoUserSession}.
     * @param deviceKey                         REQUIRED: This is the device-key assigned the new device.
     * @param passwordVerifier                  REQUIRED: Random string generated by the SDK.
     * @param salt                              REQUIRED: Generated by the SDK to set the device verifier.
     * @param deviceName                        REQUIRED: A user identifiable string assigned to the device.
     * @return {@link ConfirmDeviceResult}, service response.
     */
    private ConfirmDeviceResult confirmDeviceInternal(CognitoUserSession session, String deviceKey, String passwordVerifier, String salt, String deviceName) {
        if (session != null && session.isValid()) {
            if (deviceKey != null && deviceName != null) {
                DeviceSecretVerifierConfigType deviceConfig = new DeviceSecretVerifierConfigType();
                deviceConfig.setPasswordVerifier(passwordVerifier);
                deviceConfig.setSalt(salt);
                ConfirmDeviceRequest confirmDeviceRequest = new ConfirmDeviceRequest();
                confirmDeviceRequest.setAccessToken(session.getAccessToken().getJWTToken());
                confirmDeviceRequest.setDeviceKey(deviceKey);
                confirmDeviceRequest.setDeviceName(deviceName);
                confirmDeviceRequest.setDeviceSecretVerifierConfig(deviceConfig);
                return cognitoIdentityProviderClient.confirmDevice(confirmDeviceRequest);
            } else {
                if (deviceKey == null) {
                    throw new CognitoParameterInvalidException("Device key is null");
                } else {
                    throw new CognitoParameterInvalidException("Device name is null");
                }
            }
        } else {
            throw new CognitoNotAuthorizedException("User is not authorized");
        }
    }


    /**
     * Determines the next step from the challenge.
     * This takes an object of type {@link InitiateAuthResult} as parameter and creates an object of type
     * {@link RespondToAuthChallengeResult} and calls {@code handleChallenge(RespondToAuthChallengeResult challenge, final AuthenticationHandler callback)} method.
     *
     * @param authResult        REQUIRED: Result from the {@code initiateAuth(...)} method.
     * @param callback          REQUIRED: Callback for type {@link AuthenticationHandler}
     * @param runInBackground   REQUIRED: Boolean to indicate the current threading.
     * @return {@link Runnable} for the next step in user authentication.
     */
    private Runnable handleChallenge(final InitiateAuthResult authResult, final AuthenticationHandler callback, final boolean runInBackground) {
        try {
            RespondToAuthChallengeResult challenge = new RespondToAuthChallengeResult();
            challenge.setChallengeName(authResult.getChallengeName());
            challenge.setSession(authResult.getSession());
            challenge.setAuthenticationResult(authResult.getAuthenticationResult());
            challenge.setChallengeParameters(authResult.getChallengeParameters());
            return handleChallenge(challenge, callback, runInBackground);
        } catch (final Exception e) {
            return new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(e);
                }
            };
        }
    }

    /**
     * Performs device SRP authentication to identify remembered devices. Restarts authentication if
     * the device verification does not succeed.
     *
     * @param challenge         REQUIRED: {@link RespondToAuthChallengeResult}, contains the current challenge.
     * @param callback          REQUIRED: {@link AuthenticationHandler} callback.
     * @param runInBackground   REQUIRED: Boolean to indicate the current threading.
     * @return {@link Runnable} for the next step in user authentication.
     */
    private Runnable deviceSrpAuthentication(final RespondToAuthChallengeResult challenge, final AuthenticationHandler callback, final boolean runInBackground) {
      throw new RuntimeException("Device SRP challenge not supported!");
//        String deviceSecret = CognitoDeviceHelper.getDeviceSecret(usernameInternal, pool.getUserPoolId());
//        String deviceGroupKey = CognitoDeviceHelper.getDeviceGroupKey(usernameInternal, pool.getUserPoolId());
//        AuthenticationHelper authenticationHelper = new AuthenticationHelper(deviceGroupKey);
//        RespondToAuthChallengeRequest devicesAuthRequest = initiateDevicesAuthRequest(authenticationHelper);
//        try {
//            RespondToAuthChallengeResult initiateDeviceAuthResult = cognitoIdentityProviderClient.respondToAuthChallenge(devicesAuthRequest);
//            if (initiateDeviceAuthResult.getChallengeName().equals("DEVICE_PASSWORD_VERIFIER")) {
//                RespondToAuthChallengeRequest challengeResponse = deviceSrpAuthRequest(initiateDeviceAuthResult, deviceSecret, deviceGroupKey, authenticationHelper);
//                RespondToAuthChallengeResult deviceSRPAuthResult = cognitoIdentityProviderClient.respondToAuthChallenge(challengeResponse);
//            }
//            return handleChallenge(initiateDeviceAuthResult, callback, runInBackground);
//        } catch (final NotAuthorizedException na) {
//            final CognitoUser cognitoUser = this;
//            return clearCache(cognitoUser, runInBackground, callback);
//        } catch (final Exception e) {
//            return new Runnable() {
//                @Override
//                public void run() {
//                    callback.onFailure(e);
//                }
//            };
//        }
//    }
    }

    /**
     * Creates a authentication request to start authentication with user SRP verification.
     *
     * @param authenticationDetails     REQUIRED: {@link AuthenticationDetails}, contains details for
     *                                  user SRP authentication.
     * @param authenticationHelper      REQUIRED: Internal helper class for SRP calculations.
     * @return {@link InitiateAuthRequest}, request to start with the user SRP authentication.
     */
    private InitiateAuthRequest initiateUserSrpAuthRequest(AuthenticationDetails authenticationDetails, AuthenticationHelper authenticationHelper) {
        userId = authenticationDetails.getUserId();
        InitiateAuthRequest initiateAuthRequest = new InitiateAuthRequest();
        initiateAuthRequest.setAuthFlow("USER_SRP_AUTH");
        initiateAuthRequest.setClientId(clientId);
        initiateAuthRequest.addAuthParametersEntry("SECRET_HASH", CognitoSecretHash.getSecretHash(userId, clientId, clientSecret));
        initiateAuthRequest.addAuthParametersEntry("USERNAME", authenticationDetails.getUserId());
        initiateAuthRequest.addAuthParametersEntry("SRP_A", authenticationHelper.getA().toString(16));
        setDeviceAuthKey(initiateAuthRequest, authenticationDetails.getUserId());
        if (authenticationDetails.getValidationData() != null && authenticationDetails.getValidationData().size() > 0) {
            Map<String, String> userValidationData = new HashMap<String, String>();
            for (AttributeType attribute : authenticationDetails.getValidationData()) {
                userValidationData.put(attribute.getName(), attribute.getValue());
            }
            initiateAuthRequest.setClientMetadata(userValidationData);
        }
        return initiateAuthRequest;
    }

    /**
     * Creates a request to refresh tokens.
     *
     * @param currSession             REQUIRED: Refresh token.
     * @return  {@link InitiateAuthRequest}, request to refresh tokens.
     */
    private InitiateAuthRequest initiateRefreshTokenAuthRequest(CognitoUserSession currSession) {
        InitiateAuthRequest initiateAuthRequest = new InitiateAuthRequest();
        initiateAuthRequest.addAuthParametersEntry("REFRESH_TOKEN", currSession.getRefreshToken().getToken());
        String username = usernameInternal != null? usernameInternal : userId;
        setDeviceAuthKey(initiateAuthRequest, username);
        initiateAuthRequest.setAuthFlow("REFRESH_TOKEN");
        return initiateAuthRequest;
    }

    private void setDeviceAuthKey(InitiateAuthRequest request, String userName){
        // this is only used when we confirm a device, which happens in handleChallenge, but we
        // don't need to support that, so we just... skip it
        LOG.warn("(Fineo) Skipping DEVICE_KEY authencation parameter for InitiateAuthRequest");
//        if (deviceKey == null) {
//          LOG.error("-- not sure how to handle devicekey == null for SRP request init");
//            initiateAuthRequest.addAuthParametersEntry("DEVICE_KEY", CognitoDeviceHelper
//              .getDeviceKey(authenticationDetails.getUserId(), pool.getUserPoolId());
//        } else {
//            initiateAuthRequest.addAuthParametersEntry("DEVICE_KEY", deviceKey);
//        }
    }

    /**
     * Updates user's internal Username and device key from challenge parameters.
     *
     * @param challengeParameters           REQUIRED: Challenge parameters.
     */
    private void updateInternalUsername(Map<String, String> challengeParameters) {
        if (usernameInternal == null) {
            if (challengeParameters != null && challengeParameters.containsKey("USERNAME")) {
                usernameInternal = challengeParameters.get("USERNAME");
                deviceKey = devices.getDeviceKey(usernameInternal, getUserPoolId());
                if (secretHash == null) {
                    secretHash = CognitoSecretHash.getSecretHash(usernameInternal, clientId, clientSecret);
                }
            }
        }
    }

    /**
     * Returns the current device, if users in this pool can remember devices.
     *
     * @return {@link CognitoDevice} if the device is available, null otherwise.
     */
    public CognitoDevice thisDevice() {
        if (deviceKey == null) {
            if (usernameInternal != null) {
                deviceKey = devices.getDeviceKey(usernameInternal, getUserPoolId());
            } else if (userId != null) {
                devices.getDeviceKey(userId, getUserPoolId());
            }
        }
        if (deviceKey != null) {
            return new CognitoDevice(deviceKey, null, null, null, null, this);
        } else {
            return  null;
        }
    }

    /**
     * Private class for SRP client side math.
     */
    private static class AuthenticationHelper {
        private BigInteger a;
        private BigInteger A;
        private String poolName;

        public AuthenticationHelper(String userPoolName) {
            do {
                a = new BigInteger(EPHEMERAL_KEY_LENGTH, SECURE_RANDOM).mod(N);
                A = g.modPow(a, N);
            } while (A.mod(N).equals(BigInteger.ZERO));

            if (userPoolName.contains("_")) {
                poolName = userPoolName.split("_", 2)[1];
            } else {
                poolName = userPoolName;
            }
        }

        public BigInteger geta() {
            return a;
        }

        public BigInteger getA() {
            return A;
        }

        private static final String HEX_N =
                "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
                        + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
                        + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
                        + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                        + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
                        + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
                        + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
                        + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
                        + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"
                        + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"
                        + "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64"
                        + "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7"
                        + "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B"
                        + "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C"
                        + "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31"
                        + "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";
        private static final BigInteger N = new BigInteger(HEX_N, 16);
        private static final BigInteger g = BigInteger.valueOf(2);
        private static final BigInteger k;

        private static final int EPHEMERAL_KEY_LENGTH = 1024;
        private static final int DERIVED_KEY_SIZE = 16;
        private static final String DERIVED_KEY_INFO = "Caldera Derived Key";

        private static final ThreadLocal<MessageDigest> THREAD_MESSAGE_DIGEST =
                new ThreadLocal<MessageDigest>() {
                    @Override
                    protected MessageDigest initialValue() {
                        try {
                            return MessageDigest.getInstance("SHA-256");
                        } catch (NoSuchAlgorithmException e) {
                            throw new CognitoInternalErrorException("Exception in authentication", e);
                        }
                    }
                };

        private static final SecureRandom SECURE_RANDOM;

        static {
            try {
                SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG");

                MessageDigest messageDigest = THREAD_MESSAGE_DIGEST.get();
                messageDigest.reset();
                messageDigest.update(N.toByteArray());
                byte[] digest = messageDigest.digest(g.toByteArray());
                k = new BigInteger(1, digest);
            } catch (NoSuchAlgorithmException e) {
                throw new CognitoInternalErrorException(e.getMessage(), e);
            }
        }

        public byte[] getPasswordAuthenticationKey(String userId,
                                                   String userPassword,
                                                   BigInteger B,
                                                   BigInteger salt) {
            // Authenticate the password
            // u = H(A, B)
            MessageDigest messageDigest = THREAD_MESSAGE_DIGEST.get();
            messageDigest.reset();
            messageDigest.update(A.toByteArray());
            BigInteger u = new BigInteger(1, messageDigest.digest(B.toByteArray()));
            if (u.equals(BigInteger.ZERO)) {
                throw new CognitoInternalErrorException("Hash of A and B cannot be zero");
            }

            // x = H(salt | H(poolName | userId | ":" | password))
            messageDigest.reset();
            messageDigest.update(poolName.getBytes(StringUtils.UTF8));
            messageDigest.update(userId.getBytes(StringUtils.UTF8));
            messageDigest.update(":".getBytes(StringUtils.UTF8));
            byte [] userIdHash = messageDigest.digest(userPassword.getBytes(StringUtils.UTF8));

            messageDigest.reset();
            messageDigest.update(salt.toByteArray());
            BigInteger x = new BigInteger(1, messageDigest.digest(userIdHash));
            BigInteger S = (B.subtract(k.multiply(g.modPow(x,N))).modPow(a.add(u.multiply(x)), N)).mod(N);

            Hkdf hkdf = null;
            try {
                hkdf = Hkdf.getInstance("HmacSHA256");
            } catch (NoSuchAlgorithmException e) {
                throw new CognitoInternalErrorException(e.getMessage(), e);
            }
            hkdf.init(S.toByteArray(), u.toByteArray());
            byte[] key = hkdf.deriveKey(DERIVED_KEY_INFO, DERIVED_KEY_SIZE);
            return key;
        }
    }
}
