#!/bin/bash
keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Anverse, OU=Dev, O=Anverse, L=City, S=State, C=US" -storepass password123 -keypass password123
/opt/android-sdk/build-tools/33.0.1/apksigner sign --ks my-release-key.keystore --ks-pass pass:password123 AnversePhone.apk
