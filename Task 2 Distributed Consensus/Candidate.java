/**
 * Represents a candidate transaction broadcast by a node in the network.
 */
public class Candidate {
    /** ID of the node that sent this candidate transaction */
    public final int sender;
    
    /** The transaction proposed by the sender */
    public final Transaction tx;

    /**
     * Constructs a Candidate with the given sender ID and transaction.
     * 
     * @param sender the ID of the node sending the transaction
     * @param tx the transaction proposed by the node
     */
    public Candidate(int sender, Transaction tx) {
        this.sender = sender;
        this.tx = tx;
    }
}
