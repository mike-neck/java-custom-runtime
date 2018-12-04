#!/usr/bin/env bash

docker run --rm -v `pwd`:/project -v $HOME/.gradle:/root/.gradle -w /project mikeneck/jdk11:1 make build

