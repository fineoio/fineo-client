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

import io.fineo.client.auth.cognito.exception.CognitoParameterInvalidException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;

/**
 * Utility class for all operations on JWT.
 */
public class CognitoJWTParser {

    /**
     * Returns a claim, from the {@code JWT}s' payload, as a String.
     *
     * @param token       REQUIRED: valid JSON Web Token as String.
     * @param claim     REQUIRED: claim name as String.
     * @return  claim from the JWT as a String.
     */
    public static String getClaim(String token, String claim) {
      // some basic validation
      validateJWT(token);
      try {
        JWT jwt =  JWT.decode(token);
        Claim c = jwt.getClaim(claim);
        return c.asString();
      } catch (JWTDecodeException e) {
        throw new CognitoParameterInvalidException("Invalid JSON Web Token!", e);
      }
    }


    /**
     *  Checks if {@code JWT} is a valid JSON Web Token.
     *
     * @param JWT
     */
    public static void validateJWT(String JWT) {
        // Check if the the JWT has the three parts
        String[] jwtParts = JWT.split("\\.");
        if(jwtParts.length != 3) {
            throw new CognitoParameterInvalidException("not a JSON Web Token");
        }
    }
}
