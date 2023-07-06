package main

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"math"
	"math/rand"
	"os"
	"time"

	"google.golang.org/protobuf/proto"

	serdepb "serde/protobufs/serde"
)

type Data struct {
	Features map[string]map[string]float64 // postId -> featureName -> value
}

func genRandomString(size int) string {
	result := ""
	for k := 0; k < size; k += 1 {
		result += string('a' + rand.Intn(26))
	}
	return result
}

func serializeString(buf *[]byte, s string) {
	*buf = binary.BigEndian.AppendUint32(*buf, uint32(len(s)))
	for i := 0; i < len(s); i += 1 {
		*buf = append(*buf, s[i])
	}
}

func serializeBinary(data Data) []byte {
	buf := make([]byte, 0, 42285088)
	buf = binary.BigEndian.AppendUint32(buf, uint32(len(data.Features)))
	for postId, features := range data.Features {
		serializeString(&buf, postId)
		buf = binary.BigEndian.AppendUint32(buf, uint32(len(features)))
		for featureName, value := range features {
			serializeString(&buf, featureName)
			buf = binary.BigEndian.AppendUint64(buf, math.Float64bits(value))
		}
	}
	return buf
}

func deserializeBinary(buf []byte) Data {
	data := Data{}
	data.Features = make(map[string]map[string]float64)
	offset := 0
	featuresLen := int(binary.BigEndian.Uint32(buf[offset:]))
	offset += 4
	for i := 0; i < featuresLen; i += 1 {
		postIdLen := int(binary.BigEndian.Uint32(buf[offset:]))
		offset += 4
		postId := string(buf[offset : offset+postIdLen])
		offset += postIdLen
		features := make(map[string]float64)
		data.Features[postId] = features
		featuresLen := int(binary.BigEndian.Uint32(buf[offset:]))
		offset += 4
		for j := 0; j < featuresLen; j += 1 {
			featureNameLen := int(binary.BigEndian.Uint32(buf[offset:]))
			offset += 4
			featureName := string(buf[offset : offset+featureNameLen])
			offset += featureNameLen
			value := math.Float64frombits(binary.BigEndian.Uint64(buf[offset:]))
			offset += 8
			features[featureName] = value
		}
	}
	return data
}

func main() {
	// print argv
	if len(os.Args) < 2 {
		panic("%MODE argument expected")
	}
	mode := os.Args[1]
	if mode == "generate" {
		data := Data{
			Features: map[string]map[string]float64{},
		}
		for i := 0; i < 1000; i += 1 {
			postId := genRandomString(10)
			data.Features[postId] = make(map[string]float64)
			for j := 0; j < 1000; j += 1 {
				// generate random string id
				featureName := genRandomString(20)
				value := rand.Float64()
				data.Features[postId][featureName] = value
			}
		}
		js, err := json.Marshal(data)
		if err != nil {
			panic(err)
		}
		os.WriteFile("data.json", js, 0644)
	} else if mode == "parse_json" {
		bytes, err := os.ReadFile("data.json")
		if err != nil {
			panic(err)
		}
		var data Data
		start := time.Now()
		err = json.Unmarshal(bytes, &data)
		if err != nil {
			panic(err)
		}
		elapsed := time.Since(start)
		fmt.Printf("deserialization elapsed: %v\n", elapsed)
		start = time.Now()
		bytes2, err := json.Marshal(data)
		if err != nil {
			panic(err)
		}
		fmt.Printf("bytes2 size: %v\n", len(bytes2))
		elapsed = time.Since(start)
		fmt.Printf("serialization elapsed: %v\n", elapsed)
	} else if mode == "proto" {
		bytes, err := os.ReadFile("data.json")
		if err != nil {
			panic(err)
		}
		var data Data
		err = json.Unmarshal(bytes, &data)
		if err != nil {
			panic(err)
		}
		start := time.Now()
		out := serdepb.Data{}
		for postId, features := range data.Features {
			postData := serdepb.PostData{
				Id: postId,
			}
			for featureName, value := range features {
				postData.Features = append(postData.Features, &serdepb.Features{
					Name:  featureName,
					Value: value,
				})
			}
			out.PostData = append(out.PostData, &postData)
		}
		bytes2, err := proto.Marshal(&out)
		elapsed := time.Since(start)
		fmt.Printf("Proto serialization elapsed: %v\n", elapsed)
		fmt.Printf("bytes2 size: %v\n", len(bytes2))
		start = time.Now()
		var out2 serdepb.Data
		err = proto.Unmarshal(bytes2, &out2)
		if err != nil {
			panic(err)
		}
		elapsed = time.Since(start)
		fmt.Printf("Proto deserialization elapsed: %v\n", elapsed)
	} else if mode == "binary" {
		bytes, err := os.ReadFile("data.json")
		if err != nil {
			panic(err)
		}
		var data Data
		err = json.Unmarshal(bytes, &data)
		if err != nil {
			panic(err)
		}
		start := time.Now()
		bytes = serializeBinary(data)
		elapsed := time.Since(start)
		fmt.Printf("Binary serialization elapsed: %v\n", elapsed)
		fmt.Printf("Binary serialization bytes size: %v\n", len(bytes))
		start = time.Now()
		data2 := deserializeBinary(bytes)
		elapsed = time.Since(start)
		fmt.Printf("Binary deserialization elapsed: %v\n", elapsed)
		fmt.Printf("Binary deserialized post len: %v\n", len(data2.Features))
		js, err := json.Marshal(data2)
		if err != nil {
			panic(err)
		}
		os.WriteFile("data2.json", js, 0644)
	}
}
