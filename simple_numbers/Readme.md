This benchmark uses a naive implementation of simple number generator: for every integer i from 2 to TARGET, we check for division of this number and all known simple numbers up to sqrt(i). If none of them are divisors, add i as a simple number.  
Result - largest simple number less then TARGET.   
In the implementation, I intentionally opt out of some possible optimizations (like preallocating a vector), choosing instead the most popular solution in a target language.
### Rust
```
cargo build --release 
time target/release/simple_numbers
```
Macbook m1
Target 10m: 0.23s   
Target 100m: 4.54s   
Target 1b: 99.86s   
### C++
```
clang++ -std=c++20 -O3 -o exe main.cpp
time ./exe
```
Macbook m1
Target 10m: 0.23s   
Target 100m: 4.46s   
Target 1b: 98.35s   
### Go
```
go build -o exe .
time ./exe
```
Target 10m: 0.27s (+17%)  
Target 100m: 4.63s (+3.8%)  
Target 1b: 100.19s (+1.1%)  
Looks like there is some sort of fixed cost for initializing runtime, after that Go is almost as performant as c++. This can be explained by the fact that Go tries to use "flat" structures and put variables on stack if possible. I.e., slice of intigers is almost the same as vector in c++ - a pointer to an array of integers, with no extra indirection like in Java. The main difference I can think of is that old slice deallocation after realloc happens asynchronously by garbage collector.
### Java
```
javac Main.java
java Main
```
Target 10m: 0.90s (x3.91)   
Target 10m, x100 measures in a single run: 0.43s (x1.86)   
Target 10m, x100 measures, non-generic IntArrayList: 0.26s (+13%)  
Target 100m: 16.89s (x3.64)   
Target 1b: 370.87s (x3.71)   
Note that to achieve x1.86 for 10m target, I had to do 2 things:  
1. Create an outer loop to repeat the same calculation N=100 times (this is questionable, because who knows what sorts of calculations might end up being cached)
2. Move prime number calculation code from main body to a separate function. This is really surprising - just moving some code around between functions yielded almost x2 speed up!  

This is explained by Jit and adaptive compilation, of course. `java` call is supposed to start in "interpreter + profiling" mode that interprets jvm commands line by line and gathers some information, and after some time decides to "compile" jvm bytecode into native code. It is rather unfortunate that this "trigger" is rather moody and might not happen in a particular use-case. Even more unfortunate is the fact that before this "native" JIT compiling, there is almost no optimizations involved.  

The remaining x1.86 difference, as compared to c++, can be explained by java generics: ArrayList<Integer> has to be initialized with a wrapper-type, and will be compiled in an ArrayList structure holding some dynamic class T. That means that a) we get extra indirection for every access, because Integer is a pointer to int b) we convert Integer back to int on every iteration. Using non-generic IntArrayList from [here](https://codereview.stackexchange.com/questions/194775/high-performance-primitive-dynamic-array) sped it up to almost the same speed as in c++. This is quite unfortunate, because using generics is really convenient for code organization!  
### Kotlin
```
kotlinc Main.kt -include-runtime -d Main.jar
time java -jar Main.jar
```
Target 10m: 0.91s (x3.95)   
Target 100m: 16.32s (x3.65)    
Target 1b: 358.78s (x3.64)  
```
kotlinc-native Main.kt -o exe
time ./exe.kexe
```
Target 10m: 29.96s (x130)  

I am not sure what is going on here...