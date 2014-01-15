import org.specs2.mutable._

/**
 * @author loki
 */
class EnumTest extends Specification {

  "TestEnum" should {
    "can be converted to list" in {
      TestEnum.toList(1) mustEqual("1", "z")
      TestEnum.toList(2) mustEqual("2", "a")

      TestEnum.toSeq(1) mustEqual("1", "z")
      TestEnum.toSeq(2) mustEqual("2", "a")

      TestEnum.toMap.get("1").get mustEqual "z"
      TestEnum.toMap.get("2").get mustEqual "a"
    }
  }
}
