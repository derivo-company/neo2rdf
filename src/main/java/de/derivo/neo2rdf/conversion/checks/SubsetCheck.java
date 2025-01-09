package de.derivo.neo2rdf.conversion.checks;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SubsetCheck {

    private final Map<String, Roaring64Bitmap> idToInstancesSet;
    private final Map<String, Set<String>> subsumesRelations;

    public SubsetCheck(Map<String, Roaring64Bitmap> idToInstancesSet) {
        this.idToInstancesSet = idToInstancesSet;
        this.subsumesRelations = new HashMap<>(idToInstancesSet.size());
    }

    public Map<String, Set<String>> deriveSubsumesRelations() {
        for (Map.Entry<String, Roaring64Bitmap> subEntry : idToInstancesSet.entrySet()) {
            String subConceptID = subEntry.getKey();
            Roaring64Bitmap subInstances = subEntry.getValue();

            for (Map.Entry<String, Roaring64Bitmap> superEntry : idToInstancesSet.entrySet()) {
                String superConceptID = superEntry.getKey();
                if (Objects.equals(subConceptID, superConceptID)) {
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
