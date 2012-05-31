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

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import blackboard.sample.auth.filter.LoginAttemptCounter.LoginHistory;

/**
 * @author varju
 */
public class LoginAttemptCounterTest {
  private static final int ONE_MINUTE = 60 * 1000;

  private LoginAttemptCounter counter;

  @Before
  public void setup() {
    counter = new LoginAttemptCounter();
  }

  @Test
  public void noArgVariantOfShouldBlockCallsHelperWithCurrentTime() throws Exception {
    long before = Calendar.getInstance().getTimeInMillis();
    Thread.sleep(1);
    assertFalse(counter.shouldBlock("user"));
    Thread.sleep(1);
    long after = Calendar.getInstance().getTimeInMillis();

    LoginHistory history = counter.getHistory("user");
    assertEquals(1, history.seen.size());

    long lastSeen = history.seen.get(0);
    assertTrue(lastSeen > before);
    assertTrue(lastSeen < after);
  }

  @Test
  public void usersAreNotBlockedOnFirstAttempt() {
    assertFalse(counter.shouldBlock("user"));
  }

  @Test
  public void usersAreNotBlockedOnSecondAttempt() {
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
  }

  @Test
  public void usersAreNotBlockedOnThirdAttempt() {
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
  }

  @Test
  public void usersAreBlockedOnFourthAttempt() {
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertTrue(counter.shouldBlock("user"));
  }

  @Test
  public void usersAreStillBlockedShortlyAfterwards() {
    long firstRequestTime = 1;
    assertFalse(counter.shouldBlock("user", firstRequestTime));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 1));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 2));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 3));
  }

  @Test
  public void blockExtendsOneMinuteAfterLastAttempt() {
    long firstRequestTime = 1;
    assertFalse(counter.shouldBlock("user", firstRequestTime));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 1));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 2));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 3));

    assertTrue(counter.shouldBlock("user", firstRequestTime + 2 + ONE_MINUTE));
  }

  @Test
  public void usersAreUnblockedAutomaticallyAfterTimeout() throws Exception {
    long firstRequestTime = 1;
    assertFalse(counter.shouldBlock("user", firstRequestTime));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 1));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 2));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 3));

    assertFalse(counter.shouldBlock("user", firstRequestTime + 4 + ONE_MINUTE));
  }

  @Test
  public void userLockoutDoesntGetExtendedByRequestsDuringLockout() throws Exception {
    long firstRequestTime = 1;
    assertFalse(counter.shouldBlock("user", firstRequestTime));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 1));
    assertFalse(counter.shouldBlock("user", firstRequestTime + 2));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 3));

    assertTrue(counter.shouldBlock("user", firstRequestTime + 1000));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 2000));
    assertTrue(counter.shouldBlock("user", firstRequestTime + 3000));

    assertFalse(counter.shouldBlock("user", firstRequestTime + 4 + ONE_MINUTE));
  }

  @Test
  public void lockedUntilShowsCorrectTime() throws Exception {
    assertEquals(0, counter.lockedUntil("user"));

    long firstRequestTime = 1;
    assertFalse(counter.shouldBlock("user", firstRequestTime));
    assertEquals(0, counter.lockedUntil("user"));

    assertFalse(counter.shouldBlock("user", firstRequestTime + 1));
    assertEquals(0, counter.lockedUntil("user"));

    assertFalse(counter.shouldBlock("user", firstRequestTime + 2));
    assertEquals(0, counter.lockedUntil("user"));

    assertTrue(counter.shouldBlock("user", firstRequestTime + 3));
    assertEquals(firstRequestTime + 3 + ONE_MINUTE, counter.lockedUntil("user"));
  }

  @Test
  public void successfulLoginClearsHistory() {
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    counter.successfulLogin("user");

    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    counter.successfulLogin("user");

    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    counter.successfulLogin("user");

    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertTrue(counter.shouldBlock("user"));

    counter.successfulLogin("user");
    assertFalse(counter.shouldBlock("user"));
  }
}
