# Codegen for README CLI manual

# workflow:
# - build the project locally with maven - BUILD_DIRECTORY/target/generated-docs now contains autogenerated man pages
# - execute this script which will generate a single manual.txt file from the manual template and autogenerated man pages
# - remove manually from manual.txt:
#   - undesired newline characters that get inserted by the conversion procedure
#   - generation date
# - copy "manual.txt" into the README.md (a "git diff" might be helpful)

# adjust width to default maximum GitHub markdown code width
MANWIDTH=95
export MANWIDTH

GENERATED_DOCS_PATH=./../../target/generated-docs
BASE=$(man $GENERATED_DOCS_PATH/neo2rdf.1 | col -b) \
DUMP=$(man $GENERATED_DOCS_PATH/neo2rdf-dump.1 | col -b) \
SERVER=$(man $GENERATED_DOCS_PATH/neo2rdf-server.1 | col -b) \
envsubst < ./manual-template.txt > $GENERATED_DOCS_PATH/manual.txt

