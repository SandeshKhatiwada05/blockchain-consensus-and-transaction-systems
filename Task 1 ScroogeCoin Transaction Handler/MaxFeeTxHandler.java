import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class MaxFeeTxHandler {
    private UTXOPool utxoPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // Defensive copy of UTXO pool
        this.utxoPool = new UTXOPool(utxoPool);
    }

    // Calculate the fee of a transaction = sum(inputs) - sum(outputs)
    private double getTxFee(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output prevOut = utxoPool.getTxOutput(utxo);
            if (prevOut != null) {
                inputSum += prevOut.value;
            } else {
                // Input not found in UTXO pool — invalid input
                return -1;
            }
        }

        for (Transaction.Output out : tx.getOutputs()) {
            outputSum += out.value;
        }

        return inputSum - outputSum; // fee
    }

    // Check if transaction is valid (reusing TxHandler's isValidTx logic)
    private boolean isValidTx(Transaction tx) {
        double inputSum = 0, outputSum = 0;
        HashSet<UTXO> seen = new HashSet<>();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);

            if (!utxoPool.contains(utxo) || seen.contains(utxo)) return false;

            Transaction.Output prevOut = utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(prevOut.address, tx.getRawDataToSign(i), in.signature)) return false;

            inputSum += prevOut.value;
            seen.add(utxo);
        }

        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) return false;
            outputSum += out.value;
        }

        return inputSum >= outputSum;
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();

        // Sort transactions by fee descending
        Arrays.sort(possibleTxs, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                double fee1 = getTxFee(t1);
                double fee2 = getTxFee(t2);
                return Double.compare(fee2, fee1); // Descending order
            }
        });

        boolean changed = true;
        while (changed) {
            changed = false;

            for (Transaction tx : possibleTxs) {
                if (!acceptedTxs.contains(tx) && isValidTx(tx)) {
                    acceptedTxs.add(tx);
                    changed = true;

                    // Remove inputs (spent outputs) from pool
                    for (Transaction.Input in : tx.getInputs()) {
                        UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }

                    // Add outputs as new UTXOs
                    byte[] txHash = tx.getHash();
                    for (int i = 0; i < tx.numOutputs(); i++) {
                        UTXO utxo = new UTXO(txHash, i);
                        utxoPool.addUTXO(utxo, tx.getOutput(i));
                    }
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }
}