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

import blackboard.data.user.User;
import blackboard.platform.authentication.AbstractUsernamePasswordPostValidationCheck;
import blackboard.platform.authentication.ValidationResult;
import blackboard.platform.authentication.ValidationStatus;

/**
 * Executes after validation has been attempted for the username/password pair. If there have been too many bad login
 * attempts for this user, we'll temporarily lock their account.
 * <p>
 * Limitations:
 * <ul>
 * <li>This assumes that the username typed into the login box matches the User.userName field. If it doesn't match,
 * previous login attempts will not be cleared out until {@link LoginAttemptCounter#TIME_WINDOW_MINUTES} elapse.</li>
 * <ul>
 * 
 * @author varju
 */
public class AfterLogin extends AbstractUsernamePasswordPostValidationCheck {
  private final LoginAttemptCounter attemptCounter;

  public AfterLogin() {
    this(LoginAttemptCounter.getInstance());
  }

  public AfterLogin(LoginAttemptCounter counter) {
    attemptCounter = counter;
  }

  @Override
  public ValidationResult postValidationChecks(User user) {
    attemptCounter.successfulLogin(user.getUserName());

    ValidationResult result = new ValidationResult(null);
    result.setStatus(ValidationStatus.Continue);
    return result;
  }
}
