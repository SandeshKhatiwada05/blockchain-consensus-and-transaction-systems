import java.util.Set;

public interface Node {
    /** {@code followees[i]} is true if this node follows node {@code i} */
    void setFollowees(boolean[] followees);

    /** Initialize the set of transactions the node initially has */
    void setPendingTransaction(Set<Transaction> pendingTransactions);

    /**
     * Return the set of transactions this node proposes to send to its followers.
     * After the final round, this should return the transactions the node has reached consensus on.
     */
    Set<Transaction> sendToFollowers();

    /** Receive candidate transactions from followees */
    void receiveFromFollowees(Set<Candidate> candidates);
}
