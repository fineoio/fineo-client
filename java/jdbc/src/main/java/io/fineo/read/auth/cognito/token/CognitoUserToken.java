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

package io.fineo.read.auth.cognito.token;

/**
 * Base class for Cognito tokens.
 */
public class CognitoUserToken {
    // A Cognito Token - can be an Access, Id or Refresh token
    private String token;

    // Construct a new Cognito token
    public CognitoUserToken(String token){
        this.token = token;
    }

    protected String getToken() {
        return token;
    }

}
