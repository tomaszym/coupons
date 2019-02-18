Requirements:
 - newish scala, sbt,
 - newish docker, docker-compose,
 - ports 8080, 5432 available,

Tested on linux.

1. `docker-compose up -d`
2. `sbt http/run`
3. `./create-duels.sh`
4. configure your bets in `create-coupon.sh`
5. `./create-coupon.sh`
6. `./simulate-and-check-coupon.sh`

