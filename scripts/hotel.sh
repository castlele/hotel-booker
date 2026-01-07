#!/bin/bash

URL="localhost:8080/api/hotels"

echo $(curl -X POST $URL \
    -H "Content-Type: application/json" \
    --data '{"name":"foo","address":"bar"}')
