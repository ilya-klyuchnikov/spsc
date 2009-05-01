package spsc
import Algebra._

case class Branch(v: Var, pat: Pattern)
case class Node1(expr: Term, parent: Node1, branch: Branch) {
  def ancestors(): List[Node1] = if (parent == null) Nil else parent :: parent.ancestors
  def isProcessed: Boolean = expr match {
    case Ctr(_, Nil) => true
    case v: Var => true
    case _ => fnode != null
  }
  def fnode() = (ancestors find {n => !trivial(n.expr) && equiv(expr, n.expr)}).getOrElse(null)
}

class Tree1(val root: Node1) {
  def children(n: Node1) = List[Node1]()
  def addChildren(node: Node1, cs: List[(Term, Branch)]): Tree1 =  
    new Tree1(root) {
      val newChildren = cs map {case (t, b) => new Node1(t, node, b)}
      override def children(n: Node1) = if (node == n) newChildren else Tree1.this.children(n)
    }

  def replace(node: Node1, exp: Term) : Tree1 = 
    if (node == root) 
      new Tree1(node)
    else
      new Tree1(root) {
        val newChildren = children(node.parent) map {n => if (n == node) new Node1(exp, node.parent, node.branch) else n}
        override def children(n: Node1) = if (node == n) newChildren else Tree1.this.children(n)
      }
  
  def leafs_(node: Node1): List[Node1] = 
    if (children(node).isEmpty) List(node) else List.flatten(children(node) map leafs_)
  
  def leafs() = leafs_(root)
  
  def toString(node: Node1, indent: String): String = {
    val sb = new StringBuilder(indent + "|__" + node.expr)
    for (n <- children(node)) {
      sb.append("\n  " + indent + "|" + n.branch)
      sb.append("\n" + toString(n, indent + "  "))
    }
    sb.toString
  }
  
  override def toString = toString(root, "")
}