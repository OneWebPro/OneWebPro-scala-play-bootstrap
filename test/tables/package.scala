
/**
 * @author loki
 */
package object tables {

	import pl.onewebpro.database.Entity
	import pl.onewebpro.database.Mapper
	import play.api.db.slick.Config.driver.simple._

	case class Test(id: Option[Long], testText: String, active: Boolean) extends Entity[Test] {
		def withId(id: Long): Test = copy(id = Some(id))

		def deactivate: Test = copy(active = false)
	}

	trait TestComponent {
//		val TestTable: TestTable

		class TestTable(tag:Tag) extends Mapper[Test](tag,"test") {
			def testText = column[String]("text")

			def * = (id.?, testText, active) <>(Test.tupled, Test.unapply)
		}

	}

}
