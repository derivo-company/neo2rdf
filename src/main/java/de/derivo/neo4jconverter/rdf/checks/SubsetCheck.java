package de.derivo.neo4jconverter.rdf.checks;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SubsetCheck {

    private final Map<Long, Roaring64Bitmap> idToInstancesSet;
    private final Map<Long, Set<Long>> subsumesRelations;

    public SubsetCheck(Map<Long, Roaring64Bitmap> idToInstancesSet) {
        this.idToInstancesSet = idToInstancesSet;
        this.subsumesRelations = new HashMap<>(idToInstancesSet.size());
    }

    public Map<Long, Set<Long>> deriveSubsumesRelations() {
        for (Map.Entry<Long, Roaring64Bitmap> subEntry : idToInstancesSet.entrySet()) {
            long subConceptID = subEntry.getKey();
            Roaring64Bitmap subInstances = subEntry.getValue();

            for (Map.Entry<Long, Roaring64Bitmap> superEntry : idToInstancesSet.entrySet()) {
                long superConceptID = superEntry.getKey();
                if (subConceptID == superConceptID) {
                    continue;
                }
                Roaring64Bitmap superInstances = superEntry.getValue();

                boolean superClassContainsAll = subInstances.stream().allMatch(superInstances::contains);
                if (superClassContainsAll) {
                    this.subsumesRelations.computeIfAbsent(subConceptID, ignore -> new UnifiedSet<>())
                            .add(superConceptID);
                }
            }
        }
        return this.subsumesRelations;
    }
}
