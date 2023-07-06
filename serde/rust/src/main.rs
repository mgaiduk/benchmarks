use protobuf::{EnumOrUnknown, Message};
use serde_derive::{Deserialize, Serialize};
use std::{
    collections::HashMap,
    fs::{self, File},
    path::Path,
};

include!(concat!(env!("OUT_DIR"), "/protos/mod.rs"));
use data::{Data, Features, PostData};

#[derive(Serialize, Deserialize, Debug)]
struct JsonData {
    #[serde(rename = "Features")]
    features: HashMap<String, HashMap<String, f64>>,
}

fn main() {
    // read bytes from file
    let bytes = fs::read_to_string("data.json").unwrap();
    // measure time
    let start = std::time::Instant::now();
    let deserialized: JsonData = serde_json::from_str(&bytes).unwrap();
    println!("json deserialized in {:?}", start.elapsed());
    println!("post data len: {}", deserialized.features.len());
    let start = std::time::Instant::now();
    let serialized = serde_json::to_string(&deserialized).unwrap();
    println!("json serialized in {:?}", start.elapsed());
    println!("json serialized len: {}", serialized.len());

    // construct proto
    let start = std::time::Instant::now();
    let mut out = Data::new();
    for (key, value) in deserialized.features {
        let mut postData = PostData::new();
        for (k, v) in value {
            let mut features = Features::new();
            features.Name = k;
            features.Value = v;
            postData.Features.push(features);
        }
        out.PostData.push(postData);
    }
    let serialized = out.write_to_bytes().unwrap();
    println!("proto serialized in {:?}", start.elapsed());
    println!("proto serialized len: {}", serialized.len());
    // measure proto deserialization time
    let start = std::time::Instant::now();
    let deserialized: Data = Message::parse_from_bytes(&serialized).unwrap();
    println!("proto deserialized in {:?}", start.elapsed());
    println!("proto post data len: {}", deserialized.PostData.len());
}
