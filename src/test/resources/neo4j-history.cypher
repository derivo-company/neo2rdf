CREATE (n1:Label1:Label2:Label3 {name: 'Node with labels', someInt: 42}),
       (n2:RemovedLabel {name: 'Node without labels', someInt: 64, removedPropertyKey: 'removed value'}),
       (n1)-[:RELATION {relationName: 'node 1 to node 2', removedAnnotationPropertyKey: 'removed value'}]->(n2),
       (n1)-[:DELETED_RELATION]->(n2)
RETURN n1, n2;

MATCH (n {name: 'Node without labels'})
REMOVE n:RemovedLabel, n.removedPropertyKey
RETURN n;

MATCH (n1)-[rel:RELATION {relationName: 'node 1 to node 2'}]->(n2)
REMOVE rel.removedAnnotationPropertyKey
RETURN n1, n2;

MATCH (n1)-[rel:DELETED_RELATION]->(n2)
DELETE rel
RETURN n1, n2;
