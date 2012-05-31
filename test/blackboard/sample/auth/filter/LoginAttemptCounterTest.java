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
  private LoginAttemptCounter counter;

  @Before
  public void setup() {
    counter = new LoginAttemptCounter();
  }

  @Test
  public void usersAreNotBlockedOnFirstAttempt() {
    assertFalse(counter.shouldBlock("user"));
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
  public void callingShouldBlockTwiceAppendsToSeenList() throws Exception {
    assertFalse(counter.shouldBlock("user"));
    Thread.sleep(1);
    assertFalse(counter.shouldBlock("user"));

    LoginHistory history = counter.getHistory("user");
    assertEquals(2, history.seen.size());

    long seen1 = history.seen.get(0);
    long seen2 = history.seen.get(1);
    assertTrue(seen1 < seen2);
  }

  @Test
  public void staleEntriesAreRemovedAutomatically() throws Exception {
    long tenMinutesAgo = Calendar.getInstance().getTimeInMillis() - 10 * 60 * 1000;
    counter.shouldBlock("user", tenMinutesAgo);
    long before = Calendar.getInstance().getTimeInMillis();
    Thread.sleep(1);
    assertFalse(counter.shouldBlock("user"));

    LoginHistory history = counter.getHistory("user");
    assertEquals(1, history.seen.size());
    long lastSeen = history.seen.get(0);
    assertTrue(before < lastSeen);
  }

  @Test
  public void shouldBlockRemovesStaleFromSeenList() throws Exception {
    long tenMinutesAgo = Calendar.getInstance().getTimeInMillis() - 10 * 60 * 1000;
    counter.shouldBlock("user", tenMinutesAgo);
    long before = Calendar.getInstance().getTimeInMillis();
    Thread.sleep(1);
    assertFalse(counter.shouldBlock("user"));

    LoginHistory history = counter.getHistory("user");
    assertEquals(1, history.seen.size());
    long lastSeen = history.seen.get(0);
    assertTrue(before < lastSeen);
  }

  @Test
  public void usersAreBlockedAfterTooManyAttempts() {
    long nineMinutesAgo = Calendar.getInstance().getTimeInMillis() - 9 * 60 * 1000;
    assertFalse(counter.shouldBlock("user", nineMinutesAgo));
    assertFalse(counter.shouldBlock("user"));
    assertFalse(counter.shouldBlock("user"));
    assertTrue(counter.shouldBlock("user"));
  }

  @Test
  public void successfulLoginRemovesAllEntries() {
    counter.shouldBlock("user");
    counter.shouldBlock("user");
    assertEquals(2, counter.getHistory("user").seen.size());

    counter.successfulLogin("user");
    assertNull(counter.getHistory("user"));
  }
}
