import java.util.Arrays;

public class UTXO implements Comparable<UTXO> {

    /** Hash of the transaction from which this UTXO originates */
    private final byte[] txHash;

    /** Index of the corresponding output in the transaction */
    private final int index;

    /**
     * Creates a new UTXO corresponding to the output with given index in the transaction whose hash is txHash.
     */
    public UTXO(byte[] txHash, int index) {
        this.txHash = Arrays.copyOf(txHash, txHash.length);
        this.index = index;
    }

    /** Returns the transaction hash of this UTXO */
    public byte[] getTxHash() {
        return txHash;
    }

    /** Returns the index of this UTXO */
    public int getIndex() {
        return index;
    }

    /**
     * Checks equality by comparing transaction hash contents and output index.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        UTXO utxo = (UTXO) other;

        return index == utxo.index && Arrays.equals(txHash, utxo.txHash);
    }

    /**
     * Hash code consistent with equals.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + index;
        result = 31 * result + Arrays.hashCode(txHash);
        return result;
    }

    /**
     * Compare UTXOs by output index first, then lexicographically by transaction hash bytes.
     */
    @Override
    public int compareTo(UTXO utxo) {
        if (this.index < utxo.index) return -1;
        if (this.index > utxo.index) return 1;

        // Indices are equal, compare txHash byte arrays lexicographically
        for (int i = 0; i < Math.min(this.txHash.length, utxo.txHash.length); i++) {
            int b1 = this.txHash[i] & 0xff;
            int b2 = utxo.txHash[i] & 0xff;
            if (b1 != b2) return b1 - b2;
        }
        return this.txHash.length - utxo.txHash.length;
    }
}
