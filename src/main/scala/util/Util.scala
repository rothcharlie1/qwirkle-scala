package util

object Util {

    /*
    Returns whether the given List[A] is a strict subset of another List in a List[List[A]].
    */
    private def isNotStrictSubsetOfOther[A](l: List[A], others: List[List[A]]): Boolean = {
        others.forall(other => l.forall(other.contains) && l.size < other.size)
    }
}