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

package io.fineo.read.auth.cognito.handle;


import io.fineo.read.auth.cognito.CognitoDevice;

import java.util.List;

/**
 * Callback to get devices for a user.
 */
public interface DevicesHandler {
    /**
     * This method is called on successfully fetching devices for a user.
     * {@code device} contains all devices linked the user.
     *
     * @param devices       REQUIRED: List of all devices linked to this user.
     */
    public void onSuccess(List<CognitoDevice> devices);

    /**
     * This method is called upon encountering errors during this operation.
     * Probe {@code exception} for the cause of this exception.
     *
     * @param exception
     */
    public void onFailure(Exception exception);
}
