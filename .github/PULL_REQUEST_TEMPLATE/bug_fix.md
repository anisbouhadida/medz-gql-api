---
name: Bug fix
about: Fix a defect, regression, or incorrect behavior
---

## What changed
- Clear summary of the fix and affected components
- Related issues or tickets: <!-- e.g. Fixes #123 -->

## Why
- User impact / business context
- Technical reasoning for the fix

## Steps to reproduce
<!-- Describe the failing behavior before this PR and how to verify the fix after it is applied. -->
### Before this PR
1. 
2. 
3. 

### After this PR
1. 
2. 
3. 

## Root cause
- 

## Security impact
<!-- Keep this high level. Do not disclose exploit details or sensitive proof-of-concept steps here.
     Use SECURITY.md for private reporting when appropriate. -->
- Security impact summary:
- Input validation / auth / data handling considerations:
- Does this fix a security-sensitive issue? <!-- yes/no -->

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

### Code review guidelines
#### Security review
- Scan for input validation vulnerabilities
- Check authentication and authorization implementation
- Verify secure data handling and storage practices
- Flag hardcoded secrets or configuration issues
- Review error handling to prevent information leakage

#### Performance analysis
- Evaluate algorithmic complexity and efficiency
- Review database query optimization opportunities
- Check for potential memory leaks or resource issues
- Assess caching strategies and network call efficiency
- Identify scalability bottlenecks

#### Code quality standards
- Ensure readable, maintainable code structure
- Verify adherence to team coding standards and style guides
- Check function size, complexity, and single responsibility
- Review naming conventions and code organization
- Validate proper error handling and logging practices

#### Review communication
**Issue:** Describe what needs attention  
**Suggestion:** Provide a specific improvement, ideally with an example  
**Why:** Explain the reasoning and benefits

Use labels/emojis where helpful: 🔒 ⚡ 🧹 📚 ✅ 🚨 💭

</details>

