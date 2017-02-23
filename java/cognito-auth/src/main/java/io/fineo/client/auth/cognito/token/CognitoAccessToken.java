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

package io.fineo.client.auth.cognito.token;

import io.fineo.client.auth.cognito.exception.CognitoInternalErrorException;

import java.util.Date;

/**
 * Represents a access token and provides methods to read token claims.
 */

public class CognitoAccessToken extends CognitoUserToken {

    /**
     * Create a new access token.
     *
     * @param jwtToken      REQUIRED: Valid JWT as a String.
     */
    public CognitoAccessToken(String jwtToken) {
        super(jwtToken);
    }

    /**
     * Returns the access token formatted as JWT.
     *
     * @return
     */
    public String getJWTToken() {
        return super.getToken();
    }

    /**
     * Returns expiration of this access token.
     *
     * @return access token expiration in UTC as java.util.Date object.
     */
    public Date getExpiration() {
        try {
            String claim = CognitoJWTParser.getClaim(super.getToken(), "exp");
            if (claim == null) {
                return null;
            }
            long epocTimeSec = Long.parseLong(claim);
            long epocTimeMilliSec = epocTimeSec*1000;
            return new Date(epocTimeMilliSec);
        } catch(Exception e) {
            throw new CognitoInternalErrorException(e.getMessage());
        }
    }
}
