import java.security.*;

public class TestTxtHandler {
    public static void main(String[] args) throws Exception {
        // Generate RSA key pair (512 bits)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair kp = keyGen.generateKeyPair();
        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();

        // Step 1: Create initial UTXO pool with genesis transaction
        UTXOPool utxoPool = new UTXOPool();

        Transaction genesisTx = new Transaction();
        genesisTx.addOutput(10.0, pubKey);
        genesisTx.finalizeTx();  // Important: finalize to compute hash
        UTXO utxo = new UTXO(genesisTx.getHash(), 0);
        utxoPool.addUTXO(utxo, genesisTx.getOutput(0));

        // Step 2: Create a transaction that spends the genesis output
        Transaction tx = new Transaction();
        tx.addInput(genesisTx.getHash(), 0);
        tx.addOutput(5.0, pubKey);
        tx.addOutput(4.5, pubKey); // fee = 0.5

        // Sign the transaction input with the private key
        byte[] rawData = tx.getRawDataToSign(0);
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privKey);
        sig.update(rawData);
        byte[] signature = sig.sign();

        tx.addSignature(signature, 0);

        // Finalize transaction hash after signing
        tx.finalizeTx();

        // Step 3: Validate and handle transaction using TxHandler
        TxHandler handler = new TxHandler(utxoPool);

        System.out.println("Valid? " + handler.isValidTx(tx));

        Transaction[] result = handler.handleTxs(new Transaction[]{tx});
        System.out.println("Accepted transactions: " + result.length);
    }
}
