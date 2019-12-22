# merkle-tree

A Merkle tree library written in Java.

##Notes

**Total Number of Nodes**

*Conjecture.* Let **T(** *n* **)** be the number of nodes in a Merkle tree with *n* leaves.
Then
>  **T(** *n* **)** = 2*n* - 1

*Sketch of Proof.*

1. **T(** *n* **)** is a monontonically increasing function of *n*.
2. Suppose the number of leaves is a power of two, i.e. *n* = 2<sup>*k*</sup>.
Then the statement is true for **T(** *n* **)** = **T(** 2<sup>*k*</sup> **)** = 2<sup>*k*+1</sup> - 1 = 2*n* - 1 .
3. **T(** *n* **)** is odd.
    1. The number of '1' bits in the binary representaton of *n* uniquely determines the number of *carries* in the tree. (A *carry* is an internal Merkle tree node whose children are at *different* depths in the tree. From a breadth-first view of the tree, there is at most one carry per level and if present, it is the *rightmost* node at that level. Binary representation of *n* helps with this part of the proof.)
    2. Prove this constructively (by induction) using the parity of the number of carries starting from *n* = 2<sup>*k*</sup> + 1.
4. Combining 1 thru 3 with the pigeon hole principle, the conjecture must be true. &#x220e;

## Status

Dec 21 2009

Coming soon :)
