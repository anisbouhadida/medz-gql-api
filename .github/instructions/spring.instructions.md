---
applyTo: \*\*/\*.java, \*\*/\*.kt
description: Guidelines for building modern Spring Boot applications
  (Java 25 baseline)
---

# Spring Boot Development Guidelines (Java 25)

This document defines coding, architecture, and review standards for
Spring Boot applications using **Java 25 (LTS)** and modern Spring
ecosystem best practices.

# Platform Baseline

## Java

-   Use **Java 25 (LTS)** as the primary runtime.
-   Prefer modern language features:
    -   Pattern matching
    -   Records
    -   Sealed classes
    -   Structured concurrency
-   Avoid legacy APIs and deprecated constructs.
-   Favor immutability and functional-style code.

## Spring

-   Target:
    -   Spring Boot 3.5+ or 4.x
    -   Spring Framework 6/7+
-   Always align dependencies with Spring BOM.
-   Prefer official Spring starters over manual dependency wiring.

# General Coding Principles

-   Make only high-confidence suggestions when reviewing code.
-   Prefer readability and maintainability over cleverness.
-   Document WHY design decisions were made.
-   Handle edge cases explicitly.
-   Provide meaningful exception handling.
-   Avoid magic numbers; use constants/configuration.
-   Prefer composition to inheritance.

# Dependency Injection

-   Use constructor injection only.
-   Dependencies must be:
    -   `private`
    -   `final`
-   Avoid field injection.
-   Favor immutability.

# Configuration

-   Use `application.yml`.
-   Use Spring profiles:
    -   `dev`
    -   `test`
    -   `prod`
-   Use `@ConfigurationProperties` for type-safe configuration.
-   Externalize:
    -   Secrets
    -   Credentials
    -   Environment values
-   Never commit secrets to Git.

# Validation & Error Handling

-   Use JSR‑380 annotations (`@NotNull`, `@Size`, `@Email`).
-   Never leak stack traces externally.

# Logging & Observability

-   Use SLF4J.
-   Never use `System.out.println`.
-   Use parameterized logging.
-   Enable Actuator, Micrometer, and OpenTelemetry.

# Security

-   Use Spring Security by default.
-   Prefer OAuth2 / JWT / OIDC.
-   Use method-level security.
-   Validate ALL external inputs.
-   Prevent SQL injection via Spring Data or named parameters.

# Data & Persistence

-   Use Spring Data repositories.
-   Avoid N+1 queries.
-   Optimize fetch strategies.

# Performance

-   Monitor thread pools, GC, DB connections.
-   Consider virtual threads and AOT/native images where appropriate.

# Testing

-   Unit tests for services.
-   Slice tests (`@WebMvcTest`, `@DataJpaTest`).
-   Integration tests with Testcontainers.
-   CI must run build + tests + static analysis.