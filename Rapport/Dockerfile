# Dockerfile for building a Maven project with TeX Live support
# Pushed to https://github.com/users/R-Gld/packages/container/package/maven-with-texlive
# Base image used in .github/workflows/ci.yml
FROM maven:3.9.9-eclipse-temurin-21-alpine

# Install TeX Live
RUN apk add --no-cache texlive-full

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository"