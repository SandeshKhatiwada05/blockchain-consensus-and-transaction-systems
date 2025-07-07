import java.security.PublicKey;

public class BlockHandler {
    private BlockChain blockChain;

    /** Assume blockChain already has the genesis block */
    public BlockHandler(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    /**
     * Add {@code block} to the block chain if it is valid.
     *
     * @return true if the block is valid and has been added, false otherwise
     */
    public boolean processBlock(Block block) {
        if (block == null)
            return false;
        return blockChain.addBlock(block);
    }

    /** Create a new {@code block} over the max height {@code block} */
    public Block createBlock(PublicKey myAddress) {
        Block parent = blockChain.getMaxHeightBlock();
        if (parent == null) {
            // No blocks yet; can't create new block
            return null;
        }
        byte[] parentHash = parent.getHash();
        Block newBlock = new Block(parentHash, myAddress);

        // Get UTXO pool of max height block and current transaction pool
        UTXOPool utxoPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();

        // Create TxHandler with current UTXO pool
        TxHandler handler = new TxHandler(utxoPool);

        // Get transactions from the transaction pool
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);

        // Handle transactions: filter valid ones
        Transaction[] validTxs = handler.handleTxs(txs);

        // Add valid transactions to new block
        for (Transaction tx : validTxs) {
            newBlock.addTransaction(tx);
        }

        // Finalize block to compute its hash
        newBlock.finalizeBlock();

        // Add block to the blockchain
        if (blockChain.addBlock(newBlock))
            return newBlock;
        else
            return null;
    }

    /** Process a single transaction */
    public void processTx(Transaction tx) {
        blockChain.addTransaction(tx);
    }
}
