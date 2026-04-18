# Security Policy

## Supported versions

This project is still pre-1.0 and does not currently maintain multiple supported release branches.

Security fixes are applied to the latest code on `main`.

| Version | Supported |
| ------- | --------- |
| `main` (latest commit) | ✅ |
| Older snapshots, forks, or local modifications | ❌ |

If you discover a vulnerability that also affects an older snapshot, please still report it. The issue will be evaluated against the latest codebase first.

## Reporting a vulnerability

Please **do not open a public GitHub issue, discussion, or pull request** for suspected security vulnerabilities.

Instead, report the issue privately using one of these channels:

- Email: **anisdoescode@outlook.fr**
- Private maintainer contact: <https://github.com/anisbouhadida>

Please include as much of the following as you can:

- A clear description of the issue and the affected component
- Steps to reproduce, proof of concept, or a minimal failing request
- The potential impact and any attack prerequisites
- The commit SHA, branch, or version you tested
- Environment details relevant to reproduction
- Any suggested mitigation or patch, if you already have one

If you are unsure whether something is security-sensitive, report it privately anyway.

## What to expect after you report

We aim to:

1. Acknowledge receipt within **5 business days**
2. Confirm whether the report is in scope and reproducible
3. Share status updates while the issue is being investigated
4. Work on a fix and coordinate disclosure with you when appropriate

Resolution time depends on severity, complexity, and maintainer availability, but valid reports will be handled as quickly as possible.

## Disclosure expectations

Please allow time for investigation and remediation before making details public.

Coordinated disclosure helps protect users of this project. In practice, that means:

- Give the maintainer a reasonable opportunity to validate and fix the issue
- Avoid publishing exploit details, proof-of-concept code, or full reproduction steps before a fix or mitigation is available
- After a fix is ready, we may publish a security advisory, release notes, or other public notice describing the impact and remediation steps

If a report is accepted, credit can be given in the public disclosure unless you prefer to remain anonymous.

## Out of scope

The following are generally not treated as security vulnerabilities by themselves:

- Requests for general hardening guidance without a specific exploitable issue
- Reports that only affect unsupported forks or heavily modified local deployments
- Missing best practices without a demonstrated security impact

## Thank you

Responsible disclosure helps keep `medz-gql-api` and its users safer. Thank you for taking the time to report issues privately and responsibly.

