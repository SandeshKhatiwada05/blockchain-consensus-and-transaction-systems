import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

public class Block {

    public static final double COINBASE = 25;

    private byte[] hash;
    private byte[] prevBlockHash;
    private Transaction coinbase;
    private ArrayList<Transaction> txs;

    /** {@code address} is the address to which the coinbase transaction would go */
    public Block(byte[] prevHash, PublicKey address) {
        this.prevBlockHash = prevHash;
        this.coinbase = new Transaction(COINBASE, address);
        this.txs = new ArrayList<>();
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return txs;
    }

    public Transaction getTransaction(int index) {
        return txs.get(index);
    }

    public void addTransaction(Transaction tx) {
        txs.add(tx);
    }

    public byte[] getRawBlock() {
        ArrayList<Byte> rawBlock = new ArrayList<>();
        if (prevBlockHash != null) {
            for (byte b : prevBlockHash)
                rawBlock.add(b);
        }
        for (Transaction tx : txs) {
            byte[] rawTx = tx.getRawTx();
            for (byte b : rawTx)
                rawBlock.add(b);
        }

        byte[] raw = new byte[rawBlock.size()];
        for (int i = 0; i < raw.length; i++)
            raw[i] = rawBlock.get(i);
        return raw;
    }

    public void finalizeBlock() {
        if (hash != null) return; // Prevent re-finalization
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawBlock());
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
        }
    }
}
