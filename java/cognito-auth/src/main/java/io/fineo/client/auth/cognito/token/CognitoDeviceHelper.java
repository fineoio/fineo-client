package io.fineo.client.auth.cognito.token;

import com.amazonaws.services.cognitoidp.model.NewDeviceMetadataType;
import com.amazonaws.util.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

/**
 *
 */
public class CognitoDeviceHelper {
  final static private String COGNITO_DEVICE_CACHE = "CognitoIdentityProviderDeviceCache";

  private final Map<String, String> cache;
  static deviceSRP srpCalculator = null;

  public CognitoDeviceHelper(Map<String, String> cache) {
    this.cache = cache;
  }

  /**
   * Generates and returns the key to access device details from shared preferences.
   *
   * @param username          REQUIRED: The current user.
   * @param userPoolId        REQUIRED: Client ID of the device.
   * @return a string which is a key to access the device key from SharedPreferences.
   */
  private static String getDeviceDetailsCacheForUser(String username, String userPoolId) {
      return COGNITO_DEVICE_CACHE + "." + userPoolId + "." + username;
  }


  public void cacheDevice(String username, String userPoolId,
    NewDeviceMetadataType deviceMetadata) {
    String key = getDeviceDetailsCacheForUser(username, userPoolId)+".%s";
    String deviceKey = deviceMetadata.getDeviceKey();
    this.cache.put(format(key, "key"), deviceKey);

    this.cache.put(format(key, "group"), deviceMetadata.getDeviceGroupKey());
    Map<String, String> deviceSrpVerifiers = CognitoDeviceHelper.generateVerificationParameters(deviceKey, deviceMetadata.getDeviceGroupKey());
    this.cache.put(format(key, "secret"), deviceSrpVerifiers.get("secret"));
  }

  public String getDeviceKey(String username, String userPoolId){
    String key = getDeviceDetailsCacheForUser(username, userPoolId)+".%s";
    return this.cache.get(format(key, "key"));
  }

  /**
   * Generates SRP verification parameters for device verification.
   *
   * @param deviceKey          REQUIRED: Username this device belongs to.
   * @param deviceGroup       REQUIRED: This is the device group id returned by the service.
   * @return srp verification details for this device, as a {@link Map}.
   */
  public static Map<String, String> generateVerificationParameters(String deviceKey, String deviceGroup) {
    Map<String, String> devVerfPars = new HashMap<String, String>();
    String deviceSecret = generateRandomString();
    srpCalculator = new deviceSRP(deviceGroup, deviceKey, deviceSecret);
    byte[] salt = srpCalculator.getSalt().toByteArray();
    byte[] srpVerifier = srpCalculator.getVerifier().toByteArray();
    devVerfPars.put("salt", new String(Base64.encode(salt)));
    devVerfPars.put("verifier", new String(Base64.encode(srpVerifier)));
    devVerfPars.put("secret", deviceSecret);
    return devVerfPars;
  }

  /**
   * Static class for SRP related calculations for devices.
   */
  public static class deviceSRP {
    private BigInteger salt;
    private BigInteger verifier;
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final ThreadLocal<MessageDigest> THREAD_MESSAGE_DIGEST =
      new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
          try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
          } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
          }
        }
      };

    private static final String HEX_N = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
                                        + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
                                        + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
                                        + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                                        + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
                                        + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
                                        + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
                                        + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
                                        + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"
                                        + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"
                                        + "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64"
                                        + "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7"
                                        + "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B"
                                        + "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C"
                                        + "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31"
                                        + "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";

    private static final BigInteger N = new BigInteger(HEX_N, 16);
    private static final BigInteger g = BigInteger.valueOf(2);

    private static final SecureRandom SECURE_RANDOM;

    static {
      try {
        SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG");
      } catch (NoSuchAlgorithmException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    private static final int SALT_LENGTH_BITS = 128;

    public BigInteger getSalt() {
      return salt;
    }

    public BigInteger getVerifier() {
      return verifier;
    }

    public deviceSRP(String deviceGroupKey, String deviceKey, String password) {
      byte[] deviceKeyHash = getUserIdHash(deviceGroupKey, deviceKey, password);

      salt = new BigInteger(SALT_LENGTH_BITS, SECURE_RANDOM);
      verifier = calcVerifier(salt, deviceKeyHash);
    }

    private static BigInteger calcVerifier(BigInteger salt, byte[] userIdHash) {
      begin();
      update(salt);
      update(userIdHash);
      byte[] digest = end();

      BigInteger x = new BigInteger(1, digest);
      return g.modPow(x, N);
    }

    private byte[] getUserIdHash(String poolName, String userName, String password) {
      begin();
      update(poolName, userName, ":", password);
      return end();
    }

    public static void begin() {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      md.reset();
    }

    public static byte[] end() {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      return md.digest();
    }

    public static void update(String... strings) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      for (String s : strings) {
        if (s != null) {
          md.update(s.getBytes(StandardCharsets.UTF_8));
        }
      }
    }

    public static void update(String s) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      if (s != null) {
        md.update(s.getBytes(StandardCharsets.UTF_8));
      }
    }

    public static void update(BigInteger... bigInts) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      for (BigInteger n : bigInts) {
        if (n != null) {
          md.update(n.toByteArray());
        }
      }
    }

    public static void update(BigInteger n) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      if (n != null) {
        md.update(n.toByteArray());
      }
    }

    public static void update(ByteBuffer b) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      if (b != null) {
        md.update(b.array());
      }
    }

    public static void update(byte[] b) {
      MessageDigest md = THREAD_MESSAGE_DIGEST.get();
      if (b != null) {
        md.update(b);
      }
    }
  }

  /**
   * Returns a string with random characters.
   *
   * @return a string with random alpha-numeric characters.s
   */
  public static String generateRandomString() {
    UUID uuid = UUID.randomUUID();
    return String.valueOf(uuid);
  }
}
