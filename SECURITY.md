# **Security Policy Overview:**

- **Administrative components** have full permissions on the system. Vulnerabilities are only considered security issues if they allow **non-administrative components** (e.g., standard mods or foreign software) to bypass restrictions, execute code, or perform harmful actions.

- Any issue that allows a foreign or non-administrative component to bypass restrictions, execute code, or perform harmful actions will be considered a security issue.

- Additionally, we will accept reports of components that are intentionally designed to perform harmful actions (e.g., coremods that are essentially malware are accepted even though coremods are an administrative component).

- Any component that exists only on the remote game instance is considered a foreign component (A mod on one user's system shouldn't have permisison to do anything on another user's system, unless it's installed on both systems).

## Accepted Components
We accept security issues for all of the components listed below. If those components are not controlled by WilderForge, we will relay them privately to the relevant party if appropriate.

### Administrative components:
- Any coremod
- Wildermyth and its dependencies
- WilderForge and its dependencies. Includes, but is not limited to:
    - WilderWorkspace and its dependencies
    - Game provider and its dependencies
    - Fabric Loader and its dependencies

### Non-administrative components:
- Standard mods
- A remote game instance (eg: another player connected to the currently running game)
  
### Foreign components:
 - Issues in foreign software/components that directly impact the game or coremodding environment

## Not accepted

- Issues in foreign software/components, unless it directly impacts the game or coremodding environment
  - We encourage you to report foreign security issues to the relevant parties.

## Reporting a vulnerability

You may report a vulnerability by creating a security advisory here: https://github.com/WilderForge/WilderForge/security/advisories/new

Additionally, you may email security@wildermods.com - Please not that this email is not checked often. The fastest way to report a vulnerability is via creating a security advisory above.
