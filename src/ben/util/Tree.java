package ben.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benh on 5/22/15.
 */
public class Tree<Data> {
    private final Tree parent;
    private final Data data;
    private List<Tree<Data>> children = new ArrayList<>();

    public Tree(final Tree<Data> parent, final Data data){
        this.parent = parent;
        this.data = data;
    }

    public Tree(final Data data){
        this(null, data);
    }

    public Tree(){
        this(null, null);
    }

    public boolean isRoot(){
        return parent == null;
    }

    public boolean isLeaf(){
        return children.isEmpty();
    }

    public Tree<Data> getParent(){
        return parent;
    }

    public Data getData(){
        return data;
    }

    public List<Tree<Data>> getChildren(){
        return children;
    }

    public Tree<Data> addChild(final Data data){
        Tree<Data> result = new Tree<Data>(this, data);
        this.children.add(result);
        return result;
    }

}
