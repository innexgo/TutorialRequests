#!/bin/bash

./target/debug/innexgo-hours-service \
  --port=8080 \
  --database-url=postgres://postgres:toor@localhost/innexgo_hours \
  --site-external-url=http://localhost:3000 \
  --auth-service-url=http://localhost:8079
