import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {

    /**
     * @return true if {@code signature} is a valid digital signature of {@code message} under the
     *         key {@code pubKey}. Internally, this uses RSA signature verification.
     */
    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(message);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}
