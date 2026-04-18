---
name: Maintenance or docs
about: Refactors, dependency updates, build changes, or documentation-only work
---

## What changed
- Clear summary of modifications and affected components
- Related issues or tickets: <!-- optional -->

## Why
- Business context and requirements
- Technical reasoning for the approach taken

## Steps to validate / reproduce
<!-- For docs-only changes, describe how reviewers can verify accuracy.
     For maintenance work, include steps to reproduce the original issue or validate the updated behavior. -->
1. 
2. 
3. 

## Security impact
<!-- Keep this high level. If a dependency or hardening change addresses a vulnerability,
     avoid public exploit details and use SECURITY.md for sensitive disclosure. -->
- Security impact summary:
- Dependency / configuration / secrets considerations:
- Follow-up private reporting needed? <!-- yes/no -->

## Testing
- [ ] Unit tests pass and cover new functionality
- [ ] Manual testing completed for user-facing changes
- [ ] Performance/security considerations addressed
- [ ] I ran locally relevant checks (for example `./mvnw clean verify`)
- [ ] I updated documentation, comments, or schema/docs where needed
- [ ] If tests were not added, I explained why

## Breaking changes
- API changes or behavioral modifications:
- Migration instructions if needed:

## Deployment requirements
- [ ] Database migrations and rollback plans documented
- [ ] Environment variable updates required
- [ ] Feature flag configurations needed
- [ ] Third-party service integrations updated
- [ ] Documentation updates completed

## Reviewer focus areas
- Security:
- Performance:
- Testing:
- Documentation:
- Maintainability / readability:

<details>
<summary>Review guidance</summary>

### Review focus areas
- Prioritize changes that improve security, performance, or user experience
- Be specific and constructive in feedback
- Acknowledge good patterns and solutions
- Ask clarifying questions when code intent is unclear
- Focus on maintainability and readability improvements
- Provide migration guides for significant changes
- Update version compatibility information when relevant

### Review comment format
**Issue:** Describe what needs attention  
**Suggestion:** Provide a specific improvement, ideally with an example  
**Why:** Explain the reasoning and benefits

### Suggested labels and emojis
- 🔒 Security concerns requiring immediate attention
- ⚡ Performance issues or optimization opportunities
- 🧹 Code cleanup and maintainability improvements
- 📚 Documentation gaps or update requirements
- ✅ Positive feedback and acknowledgment of good practices
- 🚨 Critical issues that block merge
- 💭 Questions for clarification or discussion

</details>

