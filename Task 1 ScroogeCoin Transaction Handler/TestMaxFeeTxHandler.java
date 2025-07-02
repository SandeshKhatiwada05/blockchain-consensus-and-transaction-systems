import java.security.*;

public class TestMaxFeeTxHandler {
    public static void main(String[] args) throws Exception {
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair kp = keyGen.generateKeyPair();
        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();

        // Step 1: Create initial UTXO pool
        UTXOPool utxoPool = new UTXOPool();

        // Create genesis transaction with one output of 10 coins
        Transaction genesisTx = new Transaction();
        genesisTx.addOutput(10.0, pubKey);
        genesisTx.finalizeTx();

        UTXO utxo = new UTXO(genesisTx.getHash(), 0);
        utxoPool.addUTXO(utxo, genesisTx.getOutput(0));

        // Step 2: Create a transaction spending genesis output
        Transaction tx1 = new Transaction();
        tx1.addInput(genesisTx.getHash(), 0);
        tx1.addOutput(5.0, pubKey);
        tx1.addOutput(4.5, pubKey); // fee = 0.5

        // Sign input 0 of tx1
        byte[] rawData1 = tx1.getRawDataToSign(0);
        Signature sig1 = Signature.getInstance("SHA256withRSA");
        sig1.initSign(privKey);
        sig1.update(rawData1);
        byte[] signature1 = sig1.sign();
        tx1.addSignature(signature1, 0);
        tx1.finalizeTx();

        // Step 3: Create a second transaction spending the first transaction's output
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(3.0, pubKey);
        tx2.addOutput(1.5, pubKey); // fee = 0.5

        // Sign input 0 of tx2
        byte[] rawData2 = tx2.getRawDataToSign(0);
        Signature sig2 = Signature.getInstance("SHA256withRSA");
        sig2.initSign(privKey);
        sig2.update(rawData2);
        byte[] signature2 = sig2.sign();
        tx2.addSignature(signature2, 0);
        tx2.finalizeTx();

        // Put both transactions in an array
        Transaction[] possibleTxs = new Transaction[] { tx1, tx2 };

        // Step 4: Use MaxFeeTxHandler
        MaxFeeTxHandler maxFeeHandler = new MaxFeeTxHandler(utxoPool);

        // Handle transactions
        Transaction[] acceptedTxs = maxFeeHandler.handleTxs(possibleTxs);

        System.out.println("Number of accepted transactions: " + acceptedTxs.length);
        for (Transaction tx : acceptedTxs) {
            System.out.println("Accepted tx hash: " + bytesToHex(tx.getHash()));
        }
    }

    // Helper method to print bytes as hex string
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
