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

package io.fineo.client.auth.cognito.handle;

import io.fineo.client.auth.cognito.CognitoIdentityProviderContinuation;
import io.fineo.client.auth.cognito.CognitoUser;

import java.util.concurrent.RecursiveTask;

/**
 * Defines Continuation for authentication. This Continuation is used when user log-in details
 * are required to continue to authenticate the user and get tokens.
 */
public class AuthenticationContinuation implements CognitoIdentityProviderContinuation<String> {

    // Boolean constants used to indicate where this continuation will run.
    final public static boolean RUN_IN_BACKGROUND = true;
    final public static boolean RUN_IN_CURRENT = false;

    // Data required to continue with the authentication process.
    final private CognitoUser user;
    final private AuthenticationHandler callback;
    final private boolean runInBackground;

    private AuthenticationDetails authenticationDetails = null;

    /**
     * Constructs a new continuation in the authentication process.
     *
     * @param user
     * @param runInBackground
     * @param callback
     */
    /**
     * Constructs a new continuation in the authentication process.
     *
     * @param user                  REQUIRED: Reference to the {@link CognitoUser} object.
     * @param runInBackground       REQUIRED: Represents where this continuation has to run.
     * @param callback              REQUIRED: Callback to interact with the app.
     */
    public AuthenticationContinuation(CognitoUser user,
                                      boolean runInBackground,
                                      AuthenticationHandler callback) {
        this.user = user;
        this.runInBackground = runInBackground;
        this.callback = callback;
    }

    /**
     * Returns the parameters required for this continuation.
     *
     * @return
     */
    public String getParameters(){
        return "AuthenticationDetails";
    }

    /**
     * Continues the authentications process by responding to the "PASSWORD_VERIFIER" challenge with
     * username and password. Depending upon the initial call, the response call is name in the current
     * or the background thread.
     *
     */
    public void continueTask() {
        if (runInBackground) {
            new RecursiveTask<Void>(){

                @Override
                protected Void compute() {
                    Runnable next;
                    try{
                        next = user.initiateUserAuthentication(authenticationDetails,
                          callback, RUN_IN_BACKGROUND);
                    } catch (final Exception e) {
                        next = new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        };
                    }
                    Runnable nextStep = next;
                    new RecursiveTask<Void>(){

                        @Override
                        protected Void compute() {
                            nextStep.run();
                            return null;
                        }
                    }.fork().join();
                    return null;
                }
            }.fork().join();
        } else {
            Runnable nextStep;
            try {
                nextStep = user.initiateUserAuthentication(authenticationDetails, callback, RUN_IN_CURRENT);
            } catch (final Exception e) {
                nextStep = new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(e);
                    }
                };
            }
            nextStep.run();
        }
    }

    /**
     * Set details required to continue with this authentication.
     *
     * @param authenticationDetails
     */
    public void setAuthenticationDetails(AuthenticationDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
    }
}
