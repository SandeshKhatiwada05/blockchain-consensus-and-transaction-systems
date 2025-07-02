# ScroogeCoin Transaction Handler

This project implements the core transaction validation and fee optimization logic of a simple blockchain-based cryptocurrency system, inspired by the ScroogeCoin example from Stanford's Blockchain course.

## 📦 Project Structure

- `Transaction.java` — Defines the structure of a transaction with inputs and outputs.
- `UTXO.java` — Represents an Unspent Transaction Output (UTXO).
- `UTXOPool.java` — Manages a pool of available (unspent) UTXOs.
- `Crypto.java` — Provides signature verification using RSA.
- `TxHandler.java` — Validates a list of transactions and updates the UTXO pool accordingly.
- `MaxFeeTxHandler.java` *(Extra Credit)* — Selects the subset of valid transactions that maximize the total transaction fees.
- `TestTxHandler.java` — Test file to validate functionality of `TxHandler`.
- `TestMaxFeeTxHandler.java` — Test file for the fee-optimized transaction handler.

## ✅ Features

- Verifies digital signatures using RSA
- Prevents double spending
- Validates transaction outputs and balances
- Dynamically updates UTXO pool after accepting transactions
- Bonus: Selects transaction subset to maximize total fees

## 🚀 How to Compile & Run

```bash
javac *.java
java TestTxHandler
java TestMaxFeeTxHandler
📚 Based On
This implementation follows the specification provided in:
Lecture 1: ScroogeCoin – Stanford CS251: Bitcoin and Crypto Currencies
🧠 Concepts Used
•	Public Key Cryptography (RSA)
•	Digital Signatures
•	Hashing and UTXO model
•	Greedy optimization (for Max Fee selection)
•	Distributed consensus (no central authority)
