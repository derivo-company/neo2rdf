CREATE (node1:Label1:Label2
       {
         name:           'node 1',
         time:           time('12:50:35.556+01:00'),
         localTime:      localtime('12:50:35.556'),
         date:           date('1967-01-21'),
         weekDateFormat: date('+2015-W13-4'), // 2015-03-26
         localDateTime:  localdatetime('2015185T19:32:24'), // 2015-07-04T19:32:24
         dateTime:       datetime('2015-06-24T12:50:35.556+0100'),
         integer:        195,
         float:          4.2222222,
         duration:       duration('P14DT16H12M'), // 14 days, 16 hours, and 12 minutes
         cartesian3d:    point({x: 0.0, y: 4.0, z: 1.0, crs: 'cartesian-3d'}),
         geo3d:          point({x: 56.0, y: 12.0, z: 1000.0, crs: 'wgs-84-3d'}),
         intList:        [0, 1, 2, 3],
         floatList:      [0.0, 1.0, 8.0, 27.0]
       }
       ),
       (node2:Label1:Label2 {name: 'node 2'}),
       (node1)-[:RELATION {name: 'node1 to node2'}]->(node2)