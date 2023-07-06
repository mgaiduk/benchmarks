### Serde benchmark
The task is to parse a map of the form postId -> featureName -> featureValue. postId and featureName are just small random strings; featureValue is a float64.
1000 posts, 1000 features
## Golang 
### json
Data size: 40m (42285088 bytes)
deserialization elapsed: 479.710916ms
serialization elapsed: 332.503959ms

### STL proto
protoc -I=. --go_out=. data.proto

Proto serialization elapsed: 90.714875ms
bytes2 size: 33016000
Proto deserialization elapsed: 90.268292ms

### binary
Binary serialization elapsed: 57.26525ms
Binary serialization bytes size: 32018004
Binary deserialization elapsed: 81.769542ms
Binary deserialized post len: 1000

## Rust
### Json
deserialized in 119.270125ms
Data len: 1000
serialized in 62.775ms
serialized len: 42233787

### Proto
proto serialized in 47.911208ms
proto serialized len: 33004000
proto deserialized in 52.436417ms
proto post data len: 1000