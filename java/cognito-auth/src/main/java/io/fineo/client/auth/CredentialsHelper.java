package io.fineo.client.auth;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import io.fineo.client.auth.cognito.CognitoUser;
import io.fineo.client.auth.cognito.CognitoUserPool;
import io.fineo.client.auth.cognito.CognitoUserSession;
import io.fineo.client.auth.cognito.handle.AuthenticationContinuation;
import io.fineo.client.auth.cognito.handle.AuthenticationDetails;
import io.fineo.client.auth.cognito.handle.AuthenticationHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CredentialsHelper {

  public static AWSCredentialsProvider getHelper(String key, String secret) {
    if (userShape(key, secret)) {
      return getUserHelper(key, secret);
    }
    return getStaticHelper(key, secret);
  }

  public static AWSCredentialsProvider getStaticHelper(String key, String secret) {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret));
  }

  public static AWSCredentialsProvider getUserHelper(String username, String password) {
    CognitoUserPool pool = new CognitoUserPool(
      CognitoClientProperties.USER_POOL_ID, CognitoClientProperties.CLIENT_ID,
      CognitoClientProperties.SECRET);
    CognitoUser user = pool.getUser(username);
    return new UserCredentialsProvider(user, username, password);
  }

  private static boolean userShape(String username, String password) {
    return username.contains("@");
  }


  private static class UserCredentialsProvider implements AWSCredentialsProvider {
    private final CognitoUser user;
    private final String username;
    private final String password;
    private AWSCredentialsProvider delegate;

    public UserCredentialsProvider( CognitoUser user, String username, String password) {
      this.user = user;
      this.username = username;
      this.password = password;
    }

    @Override
    public AWSCredentials getCredentials() {
      if (delegate == null) {
        delegate = load();
      }
      return delegate.getCredentials();
    }

    @Override
    public void refresh() {
      delegate.refresh();
    }

    private AWSCredentialsProvider load(){
      CountDownLatch done = new CountDownLatch(1);
      AtomicReference<CognitoUserSession> sessionRef = new AtomicReference<>();
      AtomicReference<Exception> error = new AtomicReference<>();
      user.getSession(new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, Object newDevice) {
          sessionRef.set(userSession);
          done.countDown();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation,
          String UserId) {
          AuthenticationDetails authDetails = new AuthenticationDetails(username, password,
            new HashMap<>());
          authenticationContinuation.setAuthenticationDetails(authDetails);
          authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(Object mfaContinuation) {
          throw new IllegalStateException("JDBC does not support MFA based login");
        }

        @Override
        public void authenticationChallenge(Object continuation) {
          throw new IllegalStateException("JDBC does not support authentication challenges");
        }

        @Override
        public void onFailure(Exception exception) {
          error.set(exception);
          done.countDown();
        }
      });
      try {
        done.await();
        Exception e = error.get();
        if (e != null) {
          throw new RuntimeException(e);
        }
        CognitoUserSession session = sessionRef.get();
        CognitoCachingCredentialsProvider credentialsProvider = new
          CognitoCachingCredentialsProvider(CognitoClientProperties.IDENTITY_POOL_ID,
          Regions.US_EAST_1);
        Map<String, String> logins = new HashMap<>();
        String source =
          "cognito-idp.us-east-1.amazonaws.com/" + CognitoClientProperties.USER_POOL_ID;
        logins.put(source, session.getIdToken().getJWTToken());
        credentialsProvider.setLogins(logins);
        return credentialsProvider;
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted waiting for user credentials!", e);
      }
    }
  }
}
