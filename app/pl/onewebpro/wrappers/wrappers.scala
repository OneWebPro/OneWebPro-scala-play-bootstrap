package pl.onewebpro.wrappers

import scala.collection.immutable.SortedSet

/**
 * @author loki
 */

object WrappersFromString {
	implicit def stringToBoolean(value: String): Boolean = value.toLowerCase == "true" || value == "1"

	implicit def stringToInt(value: String): Int = Integer.valueOf(value).toInt

	implicit def stringToDouble(value: String): Double = java.lang.Double.valueOf(value).toDouble

	implicit def stringToFloat(value: String): Float = java.lang.Float.valueOf(value).toFloat

	implicit def stringToBigDecimal(value: String): BigDecimal = BigDecimal.apply(value)
}

object WrappersToString {
	implicit def booleanToString(value: Boolean): String = if (value) "true" else "false"

	implicit def intToString(value: Int): String = value.toString

	implicit def doubleToString(value: Double): String = value.toString

	implicit def floatToString(value: Float): String = Float.box(value).toString

	implicit def bigDecimalToString(value: BigDecimal): String = value.toString
}
