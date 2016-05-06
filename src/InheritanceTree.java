import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class InheritanceTree<T> {

  private HashMap<T, Node> map;

  private class Node {
    private T data;
    private Node parent;
    private Set<Node> children;

    public Node(T data) {
      this.data = data;
      this.parent = null;
      this.children = new HashSet<Node>();
    }
  }

  public InheritanceTree() {
    map = new HashMap<T, Node>();
  }

  public boolean put(T data, T pardata) {
    if (data == null) {
      return false;
    }
    Node n = map.get(data);
    if (n == null) {
      n = new Node(data);
      map.put(data, n);
    }
    if (n.parent != null) {
      n.parent.children.remove(n);
    }
    if (pardata != null) {
      Node p = map.get(pardata);
      if (p == null) {
        p = new Node(pardata);
        map.put(pardata, p);
      }
      if (isa(pardata, data)) {
        return false; // avoid forming loop
      }
      p.children.add(n);
      n.parent = p;
    }
    return true;
  }

  public boolean isa(T data, T pardata) {
    if (data == null || pardata == null) {
      return false;
    }
    Node n = map.get(data);
    Node p = map.get(pardata);
    if (n == null || p == null) {
      return false;
    }
    for (Node cur = n; cur != null; cur = cur.parent) {
      if (cur == p) {
        return true; // avoid forming loop
      }
    }
    return false;
  }
  
  public void print(){
    Set<T> printed = new HashSet<T>();
    for(T data : map.keySet()){
      if(!printed.contains(data)){
        Node n = map.get(data);
        while(n.parent != null){
          n = n.parent;
        }
        printR(n, 0, printed);
      }
    }
  }
  public void printR(Node n, int d, Set<T> printed){
    for(int i = 0; i<d; i++){
      System.out.print("  ");
    }
    System.out.println(n.data);
    printed.add(n.data);
    for(Node c : n.children){
      printR(c, d+1, printed);
    }
  }

}
