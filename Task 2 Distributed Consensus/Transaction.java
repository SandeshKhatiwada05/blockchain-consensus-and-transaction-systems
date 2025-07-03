import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Transaction {
    private final int id;                 // Unique ID for the transaction (for simplicity)
    private byte[] hash;                 // Cached hash

    public Transaction(int id) {
        this.id = id;
        computeHash();                   // compute hash at creation
    }

    public int getId() {
        return id;
    }

    public byte[] getHash() {
        return hash;
    }

    private void computeHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.hash = digest.digest(intToBytes(id));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    private byte[] intToBytes(int val) {
        return new byte[] {
            (byte)(val >> 24),
            (byte)(val >> 16),
            (byte)(val >> 8),
            (byte)(val)
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction other = (Transaction) obj;
        return Arrays.equals(this.hash, other.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }

    @Override
    public String toString() {
        return "Tx{id=" + id + ", hash=" + Arrays.toString(hash) + "}";
    }
}
