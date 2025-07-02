import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {

    /**
     * Verifies if {@code signature} is a valid digital signature of {@code message} under the
     * public key {@code pubKey}. Uses RSA with SHA-256.
     * 
     * @param pubKey the public key to verify the signature
     * @param message the original data bytes
     * @param signature the signature bytes to verify
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(message);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}
