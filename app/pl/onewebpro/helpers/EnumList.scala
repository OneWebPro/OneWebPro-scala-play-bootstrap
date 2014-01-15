package pl.onewebpro.helpers

import scala.Enumeration

/**
 * @author loki
 */

/**
 * Trait implements methods for enums to change it in lists for templates selects
 */
trait EnumList extends Enumeration {

  /**
   * Change enum values to SortedSet after that map it to list.
   * @return
   */
  def toList: List[(String, String)] = this.values.toList.map(e => e.id.toString -> e.toString)

  def toSeq: Seq[(String, String)] = this.toList.toSeq

  def toMap: Map[String,String] = this.toList.toMap
}

