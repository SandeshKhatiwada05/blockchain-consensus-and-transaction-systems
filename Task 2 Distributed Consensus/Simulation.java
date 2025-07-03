    import java.util.*;

    public class Simulation {
        private int numNodes;
        private double connectivityProb;
        private double maliciousProb;
        private double txCommunicationProb;
        private int numRounds;

        private Node[] nodes;
        private boolean[][] followees;
        private Random rand;

        public Simulation(int numNodes, double connectivityProb, double maliciousProb, double txCommunicationProb, int numRounds) {
            this.numNodes = numNodes;
            this.connectivityProb = connectivityProb;
            this.maliciousProb = maliciousProb;
            this.txCommunicationProb = txCommunicationProb;
            this.numRounds = numRounds;
            this.rand = new Random();

            initNetwork();
        }

        private void initNetwork() {
        nodes = new Node[numNodes];
        followees = new boolean[numNodes][numNodes];

        generateGlobalTransactions(); // ✅ Now it's safe to call

        // Trust graph creation
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    followees[i][j] = rand.nextDouble() < connectivityProb;
                }
            }
        }

        for (int i = 0; i < numNodes; i++) {
            if (rand.nextDouble() < maliciousProb) {
                nodes[i] = new MaliciousNode();
            } else {
                nodes[i] = new CompliantNode(connectivityProb, maliciousProb, txCommunicationProb, numRounds);
            }

            nodes[i].setFollowees(followees[i]);

            // Assign subset of shared transactions
            Set<Transaction> initTxs = new HashSet<>();
            for (Transaction tx : allTransactions) {
                if (rand.nextDouble() < txCommunicationProb) {
                    initTxs.add(tx);
                }
            }

            nodes[i].setPendingTransaction(initTxs);
        }
    }

        private List<Transaction> allTransactions;

        private void generateGlobalTransactions() {
            allTransactions = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                allTransactions.add(new Transaction(i));
            }
        }



        @SuppressWarnings("unchecked")
        public void run() {
            for (int round = 0; round < numRounds; round++) {
                Set<Transaction>[] proposals = (Set<Transaction>[]) new Set[numNodes];
                for (int i = 0; i < numNodes; i++) {
                    proposals[i] = nodes[i].sendToFollowers();
                    if (proposals[i] == null) {
                        proposals[i] = new HashSet<>(); // Avoid null pointer
                    }
                }

                for (int i = 0; i < numNodes; i++) {
                    Set<Candidate> received = new HashSet<>();
                    for (int j = 0; j < numNodes; j++) {
                        if (followees[i][j]) {
                            for (Transaction tx : proposals[j]) {
                                received.add(new Candidate(j, tx));
                            }
                        }
                    }
                    nodes[i].receiveFromFollowees(received);
                }
            }

            // After all rounds, print consensus for each node
            for (int i = 0; i < numNodes; i++) {
                Set<Transaction> consensusTxs = nodes[i].sendToFollowers();
                System.out.println("Node " + i + " consensus:");
                if (consensusTxs != null) {
                    for (Transaction tx : consensusTxs) {
                        System.out.println(" - Tx: " + Arrays.toString(tx.getHash())); // Or implement better toString() for Transaction
                    }
                } else {
                    System.out.println(" - No transactions.");
                }
                System.out.println();
            }
        }

        public static void main(String[] args) {
            // Example parameters: 10 nodes, 20% connectivity, 30% malicious, 10% tx communication, 10 rounds
            Simulation sim = new Simulation(10, 0.2, 0.3, 0.1, 10);
            sim.run();
        }
        
    }
