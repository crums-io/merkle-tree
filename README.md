# merkle-tree

A Merkle tree library written in Java.


## Notes


**Total Number of Nodes**

*Conjecture.* Let **T(** *n* **)** be the number of nodes in a Merkle tree with *n* leaves.
Then
>  **T(** *n* **)** = 2*n* - 1

*Sketch of Proof.*

1. **T(** *n* **)** is a monontonically increasing function of *n*.
2. Suppose the number of leaves is a power of two, i.e. *n* = 2<sup>*k*</sup>.
Then the statement is true for **T(** *n* **)** = **T(** 2<sup>*k*</sup> **)** = 2<sup>*k*+1</sup> - 1 = 2*n* - 1 .
3. **T(** *n* + 1 **)** - **T(** *n* **)** > 1
4. Combining 1 thru 3 with the pigeon hole principle, **T(** *n* + 1 **)** - **T(** *n* **)** = 2 and since **T(** 2 **)** = 3, the conjecture must be true. &#x220e;

## Status

Dec 21 2009

Coming soon :)
