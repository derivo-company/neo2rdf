CYPHER
25

CREATE (n:Item {id: 'vec1'})
SET n.embedding = vector([0.1, 0.5, 0.9], 3, FLOAT32);

CYPHER
25

CREATE (m:Item {id: 'vec2'})
SET m.embedding = vector([0.2, 0.6, 1.0], 3, FLOAT32);

CYPHER
25

CREATE (n)-[r:RELATED_TO]->(m)
SET r.scoreVec = vector([0.99, 0.01], 2, FLOAT32);

CYPHER
25

CREATE (n:Item {id: 'vec3'})
SET n.embedding = vector([1, 2, 3], 3, INT);