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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import blackboard.platform.authentication.AuthenticationEvent;
import blackboard.platform.authentication.ValidationResult;
import blackboard.platform.authentication.ValidationStatus;
import blackboard.platform.authentication.log.AuthenticationLogger;

/**
 * @author varju
 */
public class BeforeLoginTest {
  @Mock
  private AuthenticationEvent authEvent;

  private MockLoginAttemptCounter attemptCounter;
  private MockAuthenticationLogger authLogger;
  private BeforeLogin validator;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    attemptCounter = new MockLoginAttemptCounter();
    authLogger = new MockAuthenticationLogger();
    validator = new MockBeforeLogin();
  }

  @Test
  public void loginCounterCalledWithUsername() {
    validator.preValidationChecks("userasdf", "pass");
    assertEquals("userasdf", attemptCounter.usernameFromShouldBlock);
  }

  @Test
  public void checkPassesIfLoginCounterIsHappy() {
    attemptCounter.shouldBlockResult = false;
    ValidationResult result = validator.preValidationChecks("user", "pass");
    assertEquals(ValidationStatus.Continue, result.getStatus());
    assertNull(authLogger.event);
  }

  @Test
  public void checkFailsIfLoginCounterIsMad() {
    attemptCounter.shouldBlockResult = true;
    ValidationResult result = validator.preValidationChecks("user", "pass");
    assertEquals(ValidationStatus.UserDenied, result.getStatus());
    assertEquals("Account locked. Try again in a few minutes.", result.getMessage());
  }

  @Test
  public void failureLogsAuthEvent() {
    attemptCounter.shouldBlockResult = true;
    validator.preValidationChecks("fflintstone", "pass");
    assertNotNull(authLogger.event);
    assertEquals("fflintstone", authLogger.event.getUsername());
  }

  private static class MockAuthenticationLogger implements AuthenticationLogger {
    public AuthenticationEvent event;

    @Override
    public void logAuthenticationEvent(AuthenticationEvent event) {
      this.event = event;
    }

    @Override
    public void flush() {
    }
  }

  private class MockBeforeLogin extends BeforeLogin {
    public MockBeforeLogin() {
      super(attemptCounter, authLogger);
    }

    @Override
    protected AuthenticationEvent buildAuthFailedEvent(String username) {
      when(authEvent.getUsername()).thenReturn(username);
      return authEvent;
    }
  }
}
