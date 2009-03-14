package spsc;

import Util.applySub

class SuperCompiler(program: Program){
  def driveExp(expr: Expression): List[Pair[Term, Map[Variable, Term]]] = expr match {
    case v: Variable => Nil
    case Constructor(name, args) => 
      args.map((_, Map()))
    case FCall(name, args)  => {
      val fDef = program.f(name)
      List((applySub(fDef.term, Map(fDef.args zip args : _*)), Map()))
    }
    case GCall(name, Constructor(cname, cargs) :: args) => {
      val gDef = program.g(name, cname)  
      List((applySub(gDef.term, Map(((gDef.arg0.args zip cargs) ::: (gDef.args zip args)) : _*)), Map()))
    }
    case gCall @ GCall(name, (v : Variable) :: args) => 
      for (g <- program.gs(name);
        val c = Constructor(g.arg0.name, g.arg0.args.map(v => SmallLanguageTermAlgebra.nextVar));
        val sub = Map(v -> c))
        yield (driveExp(applySub(gCall, sub)).head._1, sub)
    case GCall(name, call :: args) =>
      driveExp(call).map(p => (GCall(name, p._1 :: args.map(applySub(_, p._2)) ), p._2))
    case LetExpression(term, bindings) => 
      (term, Map[Variable, Term]()) :: (for (pair <- bindings) yield Pair(pair._2, Map[Variable, Term]())).toList
  }
  
  // heart of supercompiler
  def buildProcessTree(e: Expression): ProcessTree = {
    val p = new ProcessTree(new Node(e, null, Nil))
    while (!p.isClosed) {
      val beta = p.leafs.find(!_.isProcessed).get
      if (isTrivial(beta.expr)) {
        drive(p, beta)
      } else {
        beta.ancestors.find(n1 => !isTrivial(n1.expr) && equiv(n1.expr.asInstanceOf[Term], beta.expr.asInstanceOf[Term])) match {
          case Some(alpha) => beta.repeated = alpha
          case None => beta.ancestors.find(n1 => !isTrivial(n1.expr) && inst(n1.expr.asInstanceOf[Term], beta.expr.asInstanceOf[Term])) match {
            case Some(alpha) => makeAbstraction(p, beta, alpha)
            case None => drive(p, beta)
          }
        }
      }
    }    
    p
  }
  
  def drive(t: ProcessTree, n: Node): Unit = {
    t.addChildren(n, driveExp(n.expr))
  }
  
  def makeAbstraction(t: ProcessTree, alpha: Node, beta: Node): Unit = {
    val g = sub(alpha.expr.asInstanceOf[Term], beta.expr.asInstanceOf[Term]).get
    t.replace(alpha, LetExpression(alpha.expr.asInstanceOf[Term], (Map() ++ g).toList))
  }
  
  def equiv(term1: Term, term2: Term): Boolean = inst(term1, term2) && inst(term2, term1)
  def inst(term1: Term, term2: Term): Boolean = sub(term1, term2).isDefined
  
  def sub(term1: Term, term2: Term): Option[Map[Variable, Term]] = {
    var map = Map[Variable, Term]()
    def walk(t1: Term, t2: Term): Boolean = t1 match {
      case v1: Variable => map.get(v1) match {
        case None => map = map.update(v1, t2); true
        case Some(t) => t == t2 
      }
      case _ => t1.productPrefix == t2.productPrefix && 
        t1.name == t2.name && ((t1.args zip t2.args) forall {case (a, b) => walk(a, b)})
    }
    if (walk(term1, term2)) Some(map.filter {case (a, b) => a == b}) else None
  }
  
  def isTrivial(expr: Expression): Boolean = expr match {
    case l: LetExpression => !l.bindings.isEmpty
    case c: Constructor => true
    case v: Variable => true
    case _ => false
  }
  
}