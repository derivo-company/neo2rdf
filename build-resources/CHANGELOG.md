## 1.1.0 (2024-04-29)

- New feature: By default, each Neo4j relationship is now reified in RDF by a distinct blank node.
  Option `--reifyOnlyRelationshipsWithProperties` can be used to specify that only relationships with properties will be
  reified in RDF.
- Fixed: Added double quotes around `$@` in shell script to avoid re-splitting elements (
  cf. [SC2068](https://github.com/koalaman/shellcheck/wiki/SC2068))

## 1.0.0 (2024-01-25)

- Initial public release.