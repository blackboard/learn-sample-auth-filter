/*
 * Copyright (C) 2012, Blackboard Inc. All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Blackboard Inc. nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY BLACKBOARD INC ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL BLACKBOARD INC. BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package blackboard.sample.auth.filter;

import blackboard.platform.authentication.AbstractUsernamePasswordPreValidationCheck;
import blackboard.platform.authentication.ValidationResult;
import blackboard.platform.authentication.ValidationStatus;
import blackboard.platform.log.LogServiceFactory;

/**
 * Executes before any validation is attempted for the username/password pair. This will abort the login attempt if the
 * specified user has recently had too many bad logins in a row.
 * 
 * @author varju
 */
public class BeforeLogin extends AbstractUsernamePasswordPreValidationCheck {
  private final LoginAttemptCounter attemptCounter;

  public BeforeLogin() {
    this(LoginAttemptCounter.getInstance());
  }

  public BeforeLogin(LoginAttemptCounter counter) {
    attemptCounter = counter;
  }

  @Override
  public ValidationResult preValidationChecks(String username, String password) {
    LogServiceFactory.getInstance().logError("BeforeLogin: username=" + username + ", password=" + password);

    ValidationResult result = new ValidationResult(null);
    if (attemptCounter.shouldBlock(username)) {
      result.setMessage("Account locked. Try again in a few minutes.");
      result.setStatus(ValidationStatus.UserDenied);
    } else {
      result.setStatus(ValidationStatus.Continue);
    }

    return result;
  }
}
