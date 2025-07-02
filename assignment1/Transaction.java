import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {

    /**
     * Inner class representing an input of the transaction.
     */
    public class Input {
        /** Hash of the transaction whose output is being used */
        public byte[] prevTxHash;
        /** Index of the referenced output in the previous transaction */
        public int outputIndex;
        /** Signature to prove ownership */
        public byte[] signature;

        public Input(byte[] prevHash, int index) {
            if (prevHash == null)
                prevTxHash = null;
            else
                prevTxHash = Arrays.copyOf(prevHash, prevHash.length);
            outputIndex = index;
        }

        public void addSignature(byte[] sig) {
            if (sig == null)
                signature = null;
            else
                signature = Arrays.copyOf(sig, sig.length);
        }
    }

    /**
     * Inner class representing an output of the transaction.
     */
    public class Output {
        /** Value in bitcoins */
        public double value;
        /** Recipient's public key */
        public PublicKey address;

        public Output(double v, PublicKey addr) {
            value = v;
            address = addr;
        }
    }

    /** Unique hash (id) of this transaction */
    private byte[] hash;
    /** List of inputs */
    private ArrayList<Input> inputs;
    /** List of outputs */
    private ArrayList<Output> outputs;

    public Transaction() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    /**
     * Copy constructor for Transaction.
     */
    public Transaction(Transaction tx) {
        hash = tx.hash.clone();
        inputs = new ArrayList<>(tx.inputs);
        outputs = new ArrayList<>(tx.outputs);
    }

    public void addInput(byte[] prevTxHash, int outputIndex) {
        Input in = new Input(prevTxHash, outputIndex);
        inputs.add(in);
    }

    public void addOutput(double value, PublicKey address) {
        Output op = new Output(value, address);
        outputs.add(op);
    }

    public void removeInput(int index) {
        inputs.remove(index);
    }

    public void removeInput(UTXO ut) {
        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            if (u.equals(ut)) {
                inputs.remove(i);
                return;
            }
        }
    }

    /**
     * Returns the data to be signed for input at index.
     * This includes the input info plus all outputs.
     */
    public byte[] getRawDataToSign(int index) {
        if (index >= inputs.size()) return null;

        ArrayList<Byte> sigData = new ArrayList<>();
        Input in = inputs.get(index);

        byte[] prevTxHash = in.prevTxHash;
        if (prevTxHash != null)
            for (byte b : prevTxHash)
                sigData.add(b);

        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        b.putInt(in.outputIndex);
        for (byte bVal : b.array())
            sigData.add(bVal);

        for (Output op : outputs) {
            ByteBuffer valBuffer = ByteBuffer.allocate(Double.BYTES);
            valBuffer.putDouble(op.value);
            for (byte bVal : valBuffer.array())
                sigData.add(bVal);

            byte[] addrBytes = op.address.getEncoded();
            for (byte bVal : addrBytes)
                sigData.add(bVal);
        }

        byte[] sigD = new byte[sigData.size()];
        for (int i = 0; i < sigData.size(); i++)
            sigD[i] = sigData.get(i);

        return sigD;
    }

    /**
     * Adds a signature to the input at index.
     */
    public void addSignature(byte[] signature, int index) {
        inputs.get(index).addSignature(signature);
    }

    /**
     * Returns the raw transaction data including all inputs (with signatures) and outputs.
     */
    public byte[] getRawTx() {
        ArrayList<Byte> rawTx = new ArrayList<>();

        for (Input in : inputs) {
            byte[] prevTxHash = in.prevTxHash;
            if (prevTxHash != null)
                for (byte b : prevTxHash)
                    rawTx.add(b);

            ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
            b.putInt(in.outputIndex);
            for (byte bVal : b.array())
                rawTx.add(bVal);

            byte[] signature = in.signature;
            if (signature != null)
                for (byte bVal : signature)
                    rawTx.add(bVal);
        }

        for (Output op : outputs) {
            ByteBuffer b = ByteBuffer.allocate(Double.BYTES);
            b.putDouble(op.value);
            for (byte bVal : b.array())
                rawTx.add(bVal);

            byte[] addrBytes = op.address.getEncoded();
            for (byte bVal : addrBytes)
                rawTx.add(bVal);
        }

        byte[] tx = new byte[rawTx.size()];
        for (int i = 0; i < rawTx.size(); i++)
            tx[i] = rawTx.get(i);

        return tx;
    }

    /**
     * Computes and sets the hash of the transaction using SHA-256 over raw transaction data.
     */
    public void finalizeTx() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawTx());
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
        }
    }

    public void setHash(byte[] h) {
        hash = h;
    }

    public byte[] getHash() {
        return hash;
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public ArrayList<Output> getOutputs() {
        return outputs;
    }

    public Input getInput(int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        }
        return null;
    }

    public Output getOutput(int index) {
        if (index < outputs.size()) {
            return outputs.get(index);
        }
        return null;
    }

    public int numInputs() {
        return inputs.size();
    }

    public int numOutputs() {
        return outputs.size();
    }
}
