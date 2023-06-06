import Foundation

let TARGET = 1000_000_000

print(TARGET)
var vec = Array<Int>()
for i in 2...TARGET {
    var flag = false
    for prime in vec {
        if i % prime == 0 {
            flag = true
            break
        }
        if Double(prime) > sqrt(Double(i)) {
            break
        }
    }
    if !flag {
        vec.append(i)
    }
}
print(vec.last!)
