package main

import (
	"fmt"
	"math"
)

const TARGET = 1000_000_000

func main() {
	fmt.Println(TARGET)
	vec := []int{}
	//fmt.Println(vec)
	for i := 2; i <= TARGET; i++ {
		flag := false
		for _, prime := range vec {
			//fmt.Println("prime: ", prime)
			if i%prime == 0 {
				flag = true
				break
			}
			if float64(prime) > math.Sqrt(float64(i)) {
				break
			}
		}
		if !flag {
			vec = append(vec, i)
		}
	}
	if len(vec) > 0 {
		fmt.Println(vec[len(vec)-1])
	}
}
