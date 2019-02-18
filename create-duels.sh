#!/usr/bin/env bash

curl -v http://localhost:8080/duels

curl -X POST http://localhost:8080/duels/populate
curl -X POST http://localhost:8080/duels/populate

curl -v http://localhost:8080/duels


