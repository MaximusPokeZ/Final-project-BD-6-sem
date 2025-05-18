package org.example.model;

public record Quatrifollio<A, B, C, D>(A ruleId, B fieldNameInMongo, C fieldValueInMongo, D valueDefault) {}
