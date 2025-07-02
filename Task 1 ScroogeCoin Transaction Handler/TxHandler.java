import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {
    private UTXOPool utxoPool;

    /**
     * Creates a TxHandler with a defensive copy of the given UTXOPool.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * Checks whether a transaction is valid according to the following rules:
     * 1. All outputs claimed by inputs are in the current UTXO pool.
     * 2. Signatures on each input are valid.
     * 3. No UTXO is claimed multiple times by this transaction.
     * 4. All output values are non-negative.
     * 5. The sum of input values >= sum of output values.
     */
    public boolean isValidTx(Transaction tx) {
        double inputSum = 0, outputSum = 0;
        HashSet<UTXO> seenUTXOs = new HashSet<>();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);

            // Rule 1 and 3: UTXO must be in pool and not already claimed
            if (!utxoPool.contains(utxo) || seenUTXOs.contains(utxo)) {
                return false;
            }

            Transaction.Output prevOut = utxoPool.getTxOutput(utxo);

            // Rule 2: Verify signature
            if (!Crypto.verifySignature(prevOut.address, tx.getRawDataToSign(i), in.signature)) {
                return false;
            }

            inputSum += prevOut.value;
            seenUTXOs.add(utxo);
        }

        // Rule 4 and 5: Check outputs and sums
        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) {
                return false;
            }
            outputSum += out.value;
        }

        // Rule 5: inputs cover outputs
        return inputSum >= outputSum;
    }

    /**
     * Handles an array of possible transactions, returns an array of valid transactions
     * accepted and updates the UTXO pool accordingly.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();

        boolean changed = true;
        while (changed) {
            changed = false;

            for (Transaction tx : possibleTxs) {
                if (!acceptedTxs.contains(tx) && isValidTx(tx)) {
                    acceptedTxs.add(tx);
                    changed = true;

                    // Remove spent UTXOs from pool
                    for (Transaction.Input in : tx.getInputs()) {
                        UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }

                    // Add new UTXOs from transaction outputs
                    byte[] txHash = tx.getHash();
                    for (int i = 0; i < tx.numOutputs(); i++) {
                        UTXO utxo = new UTXO(txHash, i);
                        utxoPool.addUTXO(utxo, tx.getOutput(i));
                    }
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[0]);
    }
}
