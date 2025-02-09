## 2.1.0 (2025-02-07)

- New feature: Neo2RDF now uses the OpenCypher standard. While Neo2RDF deploys the Neo4j driver, it now may also work
  with other graph databases that support the Bolt protocol, such as Memgraph.

## 2.0.0 (2025-01-09)

- Refactoring: Replaced the record storage reader with the Java Neo4j driver to support databases that use
  the new Neo4j block storage format.
  - The block storage engine is not open source and therefore could not be used in the project.
  - The conversion procedure now requires a database URI, user, and password to access the database.

## 1.2.0 (2024-09-26)

- New feature: Option `--reifyRelationships` can now be used to disable the reification of Neo4j relationships
  altogether.

- New feature: Option `--relationshipTypeReificationBlacklist` can now be used to blacklist Neo4j relationship types
  which will thus not be reified in RDF.

- Updated Java dependencies to their latest versions, including Neo4j to 5.25.1

## 1.1.0 (2024-04-29)

- New feature: By default, each Neo4j relationship is now reified in RDF by a distinct blank node.
  Option `--reifyOnlyRelationshipsWithProperties` can be used to specify that only relationships with properties will be
  reified in RDF.
- Fixed: Added double quotes around `$@` in shell script to avoid re-splitting elements (
  cf. [SC2068](https://github.com/koalaman/shellcheck/wiki/SC2068))

## 1.0.0 (2024-01-25)

- Initial public release.