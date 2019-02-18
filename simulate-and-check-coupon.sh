#!/usr/bin/env bash

curl -X POST http://localhost:8080/duels/simulate

curl http://localhost:8080/coupons \
  -H 'user-id: e4328e9f-04e6-4df0-8b8f-50f5f3917753'