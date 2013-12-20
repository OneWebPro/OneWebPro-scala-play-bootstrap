package pl.onewebpro.helpers

import scala.collection.immutable.SortedSet
import scala.Enumeration

/**
 * @author loki
 */

/**
 * Trait implements methods for enums to change it in lists for templates selects
 */
trait Enum extends Enumeration {

	/**
	 * Implement wrapper that change enum values for SortedSet
	 * @return
	 */
	def toSortedSet: SortedSet[(String, String)] = EnumWrapper.toSortedSet(this)

	/**
	 * Change enum values to SortedSet after that map it to list.
	 * @return
	 */
	def toList: List[(String, String)] = toSortedSet.toList

	/**
	 * Change enum values to SortedSet after that map it to seq.
	 * @return
	 */
	def toSeq: Seq[(String, String)] = toSortedSet.toSeq
}

object EnumWrapper {

  /**
   * Method change enumeration element to SortedSet of strings.
   * It map enumeration element using id and value (id,value) for templates selects.
   * @param enum <: Enumeration
   * @tparam T <: Enumeration
   * @return
   */
  implicit def toSortedSet[T <: Enumeration](enum: T): SortedSet[(String, String)] = {
    enum.values.map(e => e.id.toString).zip(enum.values.map(e => e.toString))
  }

}

