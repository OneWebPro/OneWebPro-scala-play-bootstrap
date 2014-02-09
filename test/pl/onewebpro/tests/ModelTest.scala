package pl.onewebpro.tests

import org.specs2.mutable._
import play.api.db.slick.Config.driver.simple._
import scala.slick.jdbc.meta.MTable
import dao._
import tables._

/**
 * @author loki
 */
class ModelTest extends Specification with GlobalTests {

	lazy val ddl = dao.DDL.ddl

	"Database" should {
		"be created" in {
			runSession {
				implicit session =>
					ddl.create
					MTable.getTables().list().isEmpty mustEqual false
			}
		}
	}
	"TestTable dao" should {
		"have all dao methods working" in {
			runSession {
				implicit session =>
					ddl.create
					val inserted = TestTable.insert(Test(None, "xxx"))
					TestTable.findAll().size mustEqual 1
					TestTable.findById(inserted.id.get).get mustEqual inserted
					inserted.testText mustEqual "xxx"
					val updated = TestTable.update(inserted.copy(testText = "zzz"))
					updated.id mustEqual inserted.id
					updated.active mustEqual inserted.active
					updated mustNotEqual inserted
					updated.testText mustNotEqual inserted.testText
					updated.testText mustEqual "zzz"
					val inActive = TestTable.upinsert(Test(None, "www", active = false))
					val updatedInactive = TestTable.upinsert(updated.copy(active = false))
					TestTable.findActive(active = false).size mustEqual 2
					TestTable.findByIdActive(updatedInactive.id.get,active = false).get mustEqual updatedInactive
					TestTable.delete(updatedInactive)
					TestTable.findActive(active = false).size mustEqual 1
					val secondInserted = TestTable.insert(Test(None, "xxx"))
					TestTable.findActive().size mustEqual 1
					TestTable.deactivate(secondInserted)
					TestTable.findActive(active = false).size mustEqual 2
			}
		}
	}
}
