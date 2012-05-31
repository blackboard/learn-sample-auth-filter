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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of recently seen login attempts. Shortcuts taken to keep this example simple:
 * <ul>
 * <li>Attempts are held in memory rather than persisting</li>
 * <li>Parameters are not configurable by the administrator</li>
 * </ul>
 * 
 * @author varju
 */
public class LoginCounter {
  /** Maximum time we'll track a login attempt for */
  private static final int TIME_WINDOW = 10 * 60 * 1000;

  /** Maximum number of login attempts allowed within this window of time */
  private static final int MAX_ATTEMPTS = 3;

   private static final LoginCounter INSTANCE = new LoginCounter();
  
   public static LoginCounter getInstance() {
   return INSTANCE;
   }

  /**
   * Determines whether to block the login attempt for this username. Also records the login attempt for future use.
   */
  public boolean shouldBlock(String username) {
    return shouldBlock(username, Calendar.getInstance().getTimeInMillis());
  }

  /** A per-user set of recent login attempts */
  private Map<String, LoginHistory> history = new ConcurrentHashMap<String, LoginHistory>();

  protected boolean shouldBlock(String username, long timestamp) {
    LoginHistory userHistory = history.get(username);
    if (null == userHistory) {
      userHistory = new LoginHistory();
      history.put(username, userHistory);
    } else {
      removeOldLoginAttempts(userHistory);
    }
    userHistory.lastSeen(timestamp);

    return (userHistory.seen.size() > MAX_ATTEMPTS);
  }

  private void removeOldLoginAttempts(LoginHistory userHistory) {
    long now = Calendar.getInstance().getTimeInMillis();
    Iterator<Long> seen = userHistory.seen.iterator();
    while (seen.hasNext()) {
      long timestamp = seen.next();
      if (timestamp + TIME_WINDOW < now) {
        seen.remove();
      }
    }
  }

  protected LoginHistory getHistory(String username) {
    return history.get(username);
  }

  protected static class LoginHistory {
    protected final List<Long> seen;

    public LoginHistory() {
      seen = Collections.synchronizedList(new ArrayList<Long>());
    }

    public LoginHistory lastSeen(long lastSeen) {
      seen.add(lastSeen);
      return this;
    }
  }
}
