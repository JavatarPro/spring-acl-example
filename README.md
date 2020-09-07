# Spring ACL example

There is an example of implementation spring ACL security.

`MenuItem` - domain object (entity)

`MenuItemRepository` - its JPA repository

`AclManager` - main ACL manager interface

`AclManagerImpl` - ACL manager implementation

`AclContext` - spring context describes all required beans

---

`SpringAclExampleAppIT` - component test that covers all methods from `AclManager`. 