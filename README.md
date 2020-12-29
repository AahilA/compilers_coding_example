# Compilers Coding Example

Due to potential academic integrity violations I am puting only snippets of my compilers project here to demonstrate my coding ability. The reason I chose this project was becase we wrote the enitre system of over 10000 lines of code from scratch and I am very proud of our work.

This repository contains highlights of our compilers project being the AST, Assembly conversion and Optimizations. I specifically want to highlight upon the optimizations such as Dead Code elimation, loop invariant code motion, copy propogation and colouring register allocation algorithm (all optimizations I either majorly wrote or incase of the last one contributed to a lot of debugging). All optimizations taken from Professor Andrew Myer's class or Modern Compiler Implementation in Java, 2nd ed. Andrew Appel and Jens Palsberg. 

In AST I would like to highlight upon the ASTIRVistor.java, which was built such that we could translate the AST nodes into IR nodes which were more useful for optimizations.

In Assemble I would like to highlight AssemblyIterator.java, which would pass though those IRNodes and select the right assembly nodes using a dynamic programming approach.

With these optimizations and our base code, we were able to somehow win the "Best Compiler Award", in Cornells Compilers Class.
