package pl.onewebpro.tests

import play.api.test._
import play.api.test.Helpers._
import play.api.db.slick.DB
import play.api.Play.current
import org.specs2.execute.AsResult
import scala.slick.jdbc.JdbcBackend.Session


trait GlobalTests {

	def runSession[T](t: (Session) => T): T = {
		running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
			DB.withSession {
				implicit s: Session =>
					t(s)
			}
		}
	}

	def run[T](t: => T): T = {
		running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
			t
		}
	}

  abstract class WithApp extends WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    override def around[T: AsResult](t: => T): org.specs2.execute.Result = super.around {
      t
    }
  }


}
