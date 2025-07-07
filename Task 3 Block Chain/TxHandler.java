import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool is a defensive copy of utxoPool
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);  // defensive copy
    }

    /**
     * Returns true if:
     *  (1) all outputs claimed by tx are in the current UTXO pool,
     *  (2) the signatures on each input of tx are valid,
     *  (3) no UTXO is claimed multiple times by tx,
     *  (4) all of tx's output values are non-negative,
     *  (5) sum of tx inputs >= sum of tx outputs.
     */
    public boolean isValidTx(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;

        HashSet<UTXO> claimedUTXOs = new HashSet<>();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1) all outputs claimed are in the UTXO pool
            if (!utxoPool.contains(utxo)) {
                return false;
            }

            Transaction.Output prevTxOut = utxoPool.getTxOutput(utxo);

            // (2) the signatures on each input of tx are valid
            PublicKey pubKey = prevTxOut.address;
            byte[] message = tx.getRawDataToSign(i);
            if (!Crypto.verifySignature(pubKey, message, input.signature)) {
                return false;
            }

            // (3) no UTXO is claimed multiple times
            if (claimedUTXOs.contains(utxo)) {
                return false;
            }
            claimedUTXOs.add(utxo);

            inputSum += prevTxOut.value;
        }

        // (4) all of tx's output values are non-negative
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }

        // (5) sum of inputs >= sum of outputs
        if (inputSum < outputSum) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions,
     * checking each transaction for correctness,
     * returning mutually valid accepted transactions,
     * and updating the current UTXO pool accordingly.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();
        boolean progress = true;

        // Repeat until no new transactions accepted in a pass
        while (progress) {
            progress = false;
            for (Transaction tx : possibleTxs) {
                if (tx == null) continue;
                if (acceptedTxs.contains(tx)) continue;

                if (isValidTx(tx)) {
                    // Accept transaction
                    acceptedTxs.add(tx);
                    updateUTXOPool(tx);
                    progress = true;
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }

    /** Update the UTXO pool with the accepted transaction */
    private void updateUTXOPool(Transaction tx) {
        // Remove consumed UTXOs
        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxoToRemove = new UTXO(input.prevTxHash, input.outputIndex);
            utxoPool.removeUTXO(utxoToRemove);
        }

        // Add new UTXOs created by this transaction's outputs
        byte[] txHash = tx.getHash();
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO utxoToAdd = new UTXO(txHash, i);
            utxoPool.addUTXO(utxoToAdd, tx.getOutput(i));
        }
    }

    /** Returns the current UTXO pool */
    public UTXOPool getUTXOPool() {
        return new UTXOPool(utxoPool); // defensive copy
    }
}
