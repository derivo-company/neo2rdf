# Codegen for README CLI manual

# adjust width to default maximum GitHub markdown code width
MANWIDTH=95
export MANWIDTH

GENERATED_DOCS_PATH=./../../target/generated-docs
BASE=$(man $GENERATED_DOCS_PATH/neo2rdf.1 | col -b) \
DUMP=$(man $GENERATED_DOCS_PATH/neo2rdf-dump.1 | col -b) \
SERVER=$(man $GENERATED_DOCS_PATH/neo2rdf-server.1 | col -b) \
envsubst < ./manual-template.txt > $GENERATED_DOCS_PATH/manual.txt

