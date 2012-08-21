package org.scalaide.play2.util

import scala.collection.mutable

class AutoHashMap[K, V](generator: K => V) extends mutable.HashMap[K, V]{
  override def default(k: K) = {
      lazy val v = generator(k)
      this.synchronized {
        get(k) match {
          case Some(v) => v
          case None => put(k, v); v
        }
      }
    }
}