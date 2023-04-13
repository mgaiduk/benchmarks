## Generating the data
```
cd gen_random_data
go run main.go
# choose your own bucket here
gsutil cp simulated_data/part* gs://mgaiduk-us-central1/simulated_data/
```
This will generate 100 files of about 150mb of random words. This takes quite some time to run, so you might want to generate only a few files and just manually copy them around.
## Hadoop
```
./gradlew jar
time hadoop jar build/libs/wordcount-1.0-SNAPSHOT.jar gs://mgaiduk-us-central1/simulated_data/part0 gs://mgaiduk-us-central1/benchmarks/wordcount/hadoop_output
```
1 file, local cluster: 111.61s user 8.20s system 137% cpu 1:27.02 total
## Ytsaurus
https://github.com/ytsaurus/ytsaurus/tree/main
### Creating local cluster
https://github.com/ytsaurus/ytsaurus/tree/8d21051a73ed065abc59c688c0a05bf6e4ac7219/yt/docker/local
```
./run_local_cluster.sh
```
After this, the cluster will be live at http://localhost:8001.
### Getting the data in
```
# getting "yt tool"
python3 -m pip install ytsaurus-client
python3 -m pip install ytsaurus-yson
# creating a directory
yt create map_node //home/tmp
# uploading our simulated data
cat part0  | awk '{print "text="$1}' | yt write --table //home/tmp/tmp1 --format dsv
# running go example
time go run main.go
```
1 file, local cluster: go run main.go  1.65s user 0.84s system 1% cpu 2:16.32 total