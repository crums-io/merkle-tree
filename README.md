# merkle-tree

A Merkle tree library written in Java.

## Maven

To use this module add this dependency in your POM file:

```
<dependency>
  <groupId>io.crums</groupId>
  <artifactId>merkle-tree</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Design Goals

An easy-to-understand, fast, tamper-proof API for building, navigating Merkle tree structures. Support for Merkle tree proofs (of existence).


## Tree Construction

I am assuming you are already familiar with the basic concept, how a parent node's hash is derived from hashing the concatenation of the hashes of its children. If not, take a quick look in [wikipedia](https://en.wikipedia.org/wiki/Merkle_tree). Here we dive a bit more into the tree's *structure*. 

For the most part a Merkle tree looks like a balanced binary tree. Nodes at the lowest level in
this tree constitute the leaf items from which the tree is deterministically derived and built. Every adjacent pair of
nodes at this level join to form a parent node at the level above: and the same pairing rule applies at successive levels above, until there is but one node remaining at the highest level.

So far, so good, but what node does a last node at a level pair with if the number of nodes at
that level is odd? Instead of a normative description, let's do it by example.

### Pre-build Stage

A Merkle tree is structurally defined by the number of items in its leaves. Suppose our tree has 89 items. It's useful to picture the leaf count in binary, here `1011001`.

<img src="https://docs.google.com/drawings/d/e/2PACX-1vTih9QQZRLOIudNUC8ZO4WLoFSBiksbLqPcGRwn-UDk5Xbfdr4HnTGv-C8HO-t7bHddKd2TVjpZj-Vz/pub?w=480&amp;h=231">


The tree above is in what I'm calling the *pre-build* stage. Excluding the root node, there are 3 unpaired nodes (marked blue)
in the pre-built tree. Note the one-to-one correspondence between `1` bits in leaf count's binary representation and the unpaired nodes.

### Completion (Build Stage)

The tree is completed by successively joining unpaired nodes at different levels and forming a parent node at one level above its left child. The process begins at the bottom and works its way to the top of the tree. A parent node constructed at this stage I'm calling a *carry* (marked amber). The built tree in our example looks like this.

<img src="https://docs.google.com/drawings/d/e/2PACX-1vSoIG26qrsT9JaL6AGoG2HZE5JP-uhAG8nEkQ1VzcGcrBwAh2S2-czIv9U-upf144erF9GS3Kkq0AED/pub?w=481&amp;h=259">

Note that carries (plural for carry) can themselves be unpaired (at their own respective levels); however as the tree is built from the bottom up, there is always at least one remaining unpaired node at a level above (since the root node at pre-build is always unpaired) with which it can pair to build a parent.



## Hash Computation

A parent node's *data* in a Merkle tree is a hash of its children's data. Order matters: it's a hash of the concatenation of the left child with the right child. This implementation prepends a leaf node's data with
a `0` [byte] and an branch (internal) node's data with a `1` per the recommendation by the Certificate Transparency to thwart the "2nd pre-image attack". This is unnecessary here, since that attack only works if the height of the tree is not known and is altogether in a different context (blockchains, double spending, etc.). Still, we employ it here a) so it'll hopefully be compatible with how other libraries do it, and b) it helps catch implementation bugs. (See `Tree.java` if you need to edit these pre-pended paddings out.)


## API

There are only a few classes in this [API](https://github.com/crums-io/merkle-tree/tree/master/src/main/java/io/crums/util/mrkl).

1. [Builder](https://github.com/crums-io/merkle-tree/blob/master/src/main/java/io/crums/util/mrkl/Builder.java) - As the name suggests you build your tree with this, adding an item at a time. The item (which is a `byte[]` array) can be anything (even empty). In most applications, it'll be a hash or signature of something else and will be consequently fixed-width. The hashing algorithm for the internal nodes is configurable.
2. [Tree](https://github.com/crums-io/merkle-tree/blob/master/src/main/java/io/crums/util/mrkl/Tree.java) - Encapsulates the tree and provides random access to its parts. Provides tree navigation thru the following class..
3. [Node](https://github.com/crums-io/merkle-tree/blob/master/src/main/java/io/crums/util/mrkl/Node.java) - A node in the tree. Instances support navigating to parent, siblings, and children--as well as random access.
4. [Proof](https://github.com/crums-io/merkle-tree/blob/master/src/main/java/io/crums/util/mrkl/Proof.java) - Encapsules a minimal object "proving" the membership of an item as a leaf in the tree.
5. [TreeIndex](https://github.com/crums-io/merkle-tree/blob/master/src/main/java/io/crums/util/mrkl/index/TreeIndex.java) - This under the hood class exposes the structure of the tree (as discussed in the section above). You might find it useful. 

With the exception of `Builder` all classes in this API are immutable and safe under concurrent access. (`Builder` too is thread safe, but unlike the other classes, it blocks.)

There's a good amount javadoc comment in the source. (Useful in IDEs like Eclipse.)

### Project Doc Site

The project doc site is [here](https://crums-io.github.io/merkle-tree/).


## Notes


**Total Number of Nodes**

*Propositon.* Let **T(** *n* **)** be the number of nodes in a Merkle tree with *n* (&ge; 2) leaves.
Then
>  **T(** *n* **)** = 2*n* - 1

*Sketch of Proof.*

1. Show that **T(** *n* + 1 **)** - **T(** *n* **)** > 0 by inspecting their respective carries. Aside: there are homomorphisms (factor groups of *n*) involving equivalent arrangements of `1` bits that map to the same total number of carries. 
2. **T(** *n* **)** is odd because every parent node has an even number of children.
3. Suppose the number of leaves is a power of two, i.e. *n* = 2<sup>*k*</sup>.
Then the statement is true for **T(** *n* **)** = **T(** 2<sup>*k*</sup> **)** = 2<sup>*k*+1</sup> - 1 = 2*n* - 1 .
4. Applying the pigeon hole principle, *n* ranging from 2<sup>*k*</sup> to 2<sup>*k*+1</sup> constrained by (1) and (2), conclude that **T(** *n* + 1 **)** - **T(** *n* **)** = 2 and since **T(** 2 **)** = 3, the proposition must be true. &#x220e;



**Transition To ByteBuffers**

The current design exposes data as byte arrays. This necessitates copying the data, since there are is no such thing as a read-only byte array in Java. I resisted using read-only ByteBuffers since a) the data chunks are typically only 32 bytes wide and b) you can't compose a read-only buffer from a bunch of other read-only buffers without copying (a design limitation, imo). Still, a buffer design would be better. Next version, hopefully.

## Status

Sept 8 2022

> 1.0.0 Released. Available on Maven Central.

Feb 11 2021

> 0.0.2 Released under LGPL.

Dec 25 2019

> 0.0.1 Released. More serialization support in next version.

Dec 21 2019

> Coming soon :)
