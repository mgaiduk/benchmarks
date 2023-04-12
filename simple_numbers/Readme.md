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
Target 10m: 0.27s
Target 100m: 4.63s
Target 1b: 100.19s