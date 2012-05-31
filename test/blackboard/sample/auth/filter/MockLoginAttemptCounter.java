package blackboard.sample.auth.filter;

public class MockLoginAttemptCounter extends LoginAttemptCounter {
  public boolean shouldBlockResult;
  public String usernameFromShouldBlock;
  public String usernameFromSuccessfulLogin;

  @Override
  public boolean shouldBlock(String username) {
    usernameFromShouldBlock = username;
    return shouldBlockResult;
  }

  @Override
  public void successfulLogin(String username) {
    usernameFromSuccessfulLogin = username;
  }
}