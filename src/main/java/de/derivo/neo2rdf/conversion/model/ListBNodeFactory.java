package de.derivo.neo2rdf.conversion.model;

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

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.base.AbstractValueFactory;

import java.util.concurrent.atomic.AtomicLong;

public class ListBNodeFactory extends AbstractValueFactory {

    private String currentListHeadID = "";
    private final AtomicLong currentListElementID = new AtomicLong(0);

    public String getCurrentListHeadID() {
        return currentListHeadID;
    }

    public void setCurrentListHeadID(String currentListHeadID) {
        this.currentListHeadID = currentListHeadID;
    }

    public AtomicLong getCurrentListElementID() {
        return currentListElementID;
    }

    @Override
    public BNode createBNode(String nodeID) {
        return super.createBNode(nodeID);
    }

    @Override
    public BNode createBNode() {
        return createBNode(currentListHeadID + "_" + currentListElementID.getAndIncrement());
    }
}
