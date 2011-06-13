//
// typica - A client library for Amazon Web Services
// Copyright (C) 2008 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.common;

import com.xerox.amazonws.fps.FPSError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper exception to simplify catching errors related to AWS activity.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class AWSException extends Exception {
  private static final String UNKNOWN_REQUEST = "Unknown Request";

  private final String requestId;
  private final List<AWSError> errors = new ArrayList<AWSError>();

  public AWSException(String s) {
    super(s);
    requestId = UNKNOWN_REQUEST;
  }

  public AWSException(String s, Throwable cause) {
    super(s, cause);
    requestId = UNKNOWN_REQUEST;
  }

  public AWSException(@NotNull String message,
                      @NotNull final String requestId,
                      @NotNull AWSError... errors) {
    this(message, requestId, Arrays.asList(errors));
  }

  public AWSException(String s, String requestId, List<? extends AWSError> errors) {
    super(s);
    this.requestId = requestId;
    this.errors.addAll(errors);
  }

  protected AWSException(AWSException ex) {
    // copy constructor
    super(ex.getMessage(), ex.getCause());
    this.requestId = ex.getRequestId();
    this.errors.addAll(ex.errors);
  }

  @Nullable
  public String getRequestId() {
    return requestId;
  }

  @NotNull
  public List<? extends AWSError> getErrors() {
    return errors;
  }

  protected static String concatenateErrors(List<FPSError> errors) {
    final StringBuilder buffer = new StringBuilder();
    for (FPSError error : errors)
      buffer.append(error.getCode()).append(": ").append(error.getMessage()).append('.');
    return buffer.toString();
  }
}

