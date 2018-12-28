package de.splotycode.davidlib.domparsing.keyvalue.dom;

import lombok.Getter;
import me.david.davidlib.datafactory.DataFactory;
import me.david.davidlib.datafactory.DataKey;
import me.david.davidlib.storage.Document;
import me.david.davidlib.storage.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeyValueDocument implements Document {

    @Getter private DataFactory metaData = new DataFactory();
    private List<Node> nodes = new ArrayList<>();

    @Override
    public <T> T getMetaData(DataKey<T> key) {
        return metaData.getData(key);
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    @Override
    public Node getNode(String name) {
        Node key = nodes.stream().filter(node -> node.name().equals(name)).findFirst().orElse(null);
        if (key == null) return null;
        return key.childs().iterator().next();
    }
}