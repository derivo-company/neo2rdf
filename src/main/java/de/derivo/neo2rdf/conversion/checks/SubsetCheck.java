package de.derivo.neo2rdf.conversion.checks;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
