#!/usr/bin/env bash

curl -X POST \
  http://localhost:8080/coupons \
  -H 'Content-Type: application/json' \
  -H 'user-id: e4328e9f-04e6-4df0-8b8f-50f5f3917753' \
  -d '{
	"bets": [
			{ "duelId": "98a31b99-98fa-4ec2-887e-a7cf169c72ba", "winner": null, "rate": 3.6}
	]
}'

curl -X POST http://localhost:8080/duels/simulate

curl http://localhost:8080/coupons \
  -H 'user-id: e4328e9f-04e6-4df0-8b8f-50f5f3917753'