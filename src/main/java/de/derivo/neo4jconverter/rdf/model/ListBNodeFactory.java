package de.derivo.neo4jconverter.rdf.model;

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
