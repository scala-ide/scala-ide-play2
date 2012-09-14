package org.scalaide.play2.util

import scala.collection.mutable

/**
 * A hash map which if does not have a value for key, it will create
 * it automatically using generator method
 */
class AutoHashMap[K, V](generator: K => V) extends mutable.HashMap[K, V] {
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