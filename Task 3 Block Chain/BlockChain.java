import java.util.*;

public class BlockChain {

    public static final int CUT_OFF_AGE = 10;

    // Inner class to keep block info and associated data
    private class BlockNode {
        Block block;
        BlockNode parent;
        int height;
        UTXOPool utxoPool;

        BlockNode(Block block, BlockNode parent, UTXOPool utxoPool, int height) {
            this.block = block;
            this.parent = parent;
            this.utxoPool = utxoPool;
            this.height = height;
        }
    }

    private HashMap<ByteArrayWrapper, BlockNode> blockChainMap;  // map block hash -> BlockNode
    private TransactionPool transactionPool;
    private BlockNode maxHeightNode;

    /** Create a new blockchain with just the genesis block */
    public BlockChain(Block genesisBlock) {
        blockChainMap = new HashMap<>();
        transactionPool = new TransactionPool();

        // Create UTXOPool with coinbase tx of genesis block
        UTXOPool genesisUTXOPool = new UTXOPool();

        // Add coinbase transaction outputs to UTXO pool
        Transaction coinbaseTx = genesisBlock.getCoinbase();
        UTXO coinbaseUTXO = new UTXO(coinbaseTx.getHash(), 0);
        genesisUTXOPool.addUTXO(coinbaseUTXO, coinbaseTx.getOutput(0));

        // Create the genesis block node with height 1
        BlockNode genesisNode = new BlockNode(genesisBlock, null, genesisUTXOPool, 1);
        ByteArrayWrapper hashKey = new ByteArrayWrapper(genesisBlock.getHash());
        blockChainMap.put(hashKey, genesisNode);
        maxHeightNode = genesisNode;
    }

    /** Get the block at maximum height */
    public Block getMaxHeightBlock() {
        return maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return new UTXOPool(maxHeightNode.utxoPool); // Defensive copy
    }

    /** Get the transaction pool */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add block to the blockchain if valid:
     *  - Must have a parent block in the blockchain
     *  - Height > maxHeight - CUT_OFF_AGE
     *  - All transactions valid
     */
    public boolean addBlock(Block block) {
        if (block == null) return false;

        byte[] prevHash = block.getPrevBlockHash();

        // Reject if no parent (genesis block already exists)
        if (prevHash == null) return false;

        ByteArrayWrapper parentHashWrapper = new ByteArrayWrapper(prevHash);
        BlockNode parentNode = blockChainMap.get(parentHashWrapper);

        // Parent must exist
        if (parentNode == null) return false;

        int newHeight = parentNode.height + 1;
        if (newHeight <= maxHeightNode.height - CUT_OFF_AGE) return false;

        // Validate all transactions
        UTXOPool utxoPoolCopy = new UTXOPool(parentNode.utxoPool);
        TxHandler txHandler = new TxHandler(utxoPoolCopy);

        // Include coinbase tx
        Transaction coinbaseTx = block.getCoinbase();

        // Transactions to validate (excluding coinbase)
        ArrayList<Transaction> blockTxs = block.getTransactions();
        Transaction[] txArray = blockTxs.toArray(new Transaction[0]);

        Transaction[] validTxs = txHandler.handleTxs(txArray);

        // Check all txs are valid (no missing tx)
        if (validTxs.length != txArray.length) return false;

        // Now add coinbase tx to UTXOPool
        UTXO coinbaseUTXO = new UTXO(coinbaseTx.getHash(), 0);
        utxoPoolCopy.addUTXO(coinbaseUTXO, coinbaseTx.getOutput(0));

        // Create new BlockNode
        BlockNode newNode = new BlockNode(block, parentNode, utxoPoolCopy, newHeight);
        ByteArrayWrapper blockHashWrapper = new ByteArrayWrapper(block.getHash());
        blockChainMap.put(blockHashWrapper, newNode);

        // Update maxHeightNode if needed
        if (newHeight > maxHeightNode.height) {
            maxHeightNode = newNode;
        } else if (newHeight == maxHeightNode.height) {
            // If equal height, keep the older block (i.e., do nothing)
        }

        // Remove transactions in this block from transaction pool
        for (Transaction tx : blockTxs) {
            transactionPool.removeTransaction(tx.getHash());
        }

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }
}
