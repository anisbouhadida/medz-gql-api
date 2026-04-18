---
name: Feature or enhancement
about: Propose a new feature or improve existing behavior
---

## What changed
- Clear summary of modifications and affected components
- Related issues or tickets: <!-- e.g. Closes #123 -->

## Why
- Business context and requirements
- Technical reasoning for the approach taken

## Steps to validate / reproduce
<!-- If this change fixes a bug or regression, include exact steps to reproduce the original issue.
     Otherwise, explain how reviewers can exercise the new behavior locally or in GraphiQL. -->
1. 
2. 
3. 

## Security impact
<!-- Keep this high level. Do not include exploit details or sensitive reproduction steps in a public PR.
     If this change addresses a suspected vulnerability, coordinate private disclosure per SECURITY.md. -->
- Security impact summary:
- Input validation / auth / data handling considerations:
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
<!-- Call out any areas where reviewers should spend extra attention. -->
- Security:
- Performance:
- Testing:
- Documentation:
- Maintainability / readability:

<details>
<summary>Review guidance</summary>

### Review focus areas
- **Security**: Check for hardcoded secrets, input validation, auth issues
- **Performance**: Look for database query problems, inefficient loops
- **Testing**: Ensure adequate test coverage for new functionality
- **Documentation**: Verify code comments and README updates

### Review style
- Be specific and constructive in feedback
- Acknowledge good patterns and solutions
- Ask clarifying questions when code intent is unclear
- Focus on maintainability and readability improvements
- Prioritize changes that improve security, performance, or user experience
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

