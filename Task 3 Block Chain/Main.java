import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Main {

    public static void main(String[] args) {

        try {
            // 1. Generate a RSA key pair for testing
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512);  // 512-bit key for quick generation
            KeyPair pair = keyGen.generateKeyPair();

            PublicKey publicKey = pair.getPublic();

            // 2. Create a genesis block (no previous hash)
            Block genesisBlock = new Block(null, publicKey);
            genesisBlock.finalizeBlock();


            // 3. Create blockchain with the genesis block
            BlockChain blockchain = new BlockChain(genesisBlock);

            System.out.println("Genesis block hash: " + bytesToHex(genesisBlock.getHash()));

            // 4. Create BlockHandler for processing blocks and transactions
            BlockHandler blockHandler = new BlockHandler(blockchain);

            // 5. Create a new block over the genesis block
            Block newBlock = blockHandler.createBlock(publicKey);

            if (newBlock != null) {
                System.out.println("New block created at height: " + blockchain.getMaxHeightBlock());
                System.out.println("New block hash: " + bytesToHex(newBlock.getHash()));
            } else {
                System.out.println("Failed to create new block");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert bytes to hex string for printing hashes
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
