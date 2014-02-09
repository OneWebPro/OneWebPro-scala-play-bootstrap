import pl.onewebpro.database.{Entity, DatabaseDAO}
import play.api.db.slick.Config.driver.simple._
import tables.{Test, TestComponent}

/**
 * @author loki
 */
package object dao {

	private[dao] trait DAO[Element <: Entity[Element]] extends DatabaseDAO[Element] with DaoStructure

	trait DaoStructure extends TestComponent {
		lazy val TestTable = TableQuery[TestTable]
	}

	object DDL extends DaoStructure {
		lazy val ddl = TestTable.ddl
	}

	object TestTable extends DAO[Test] {
		val self = TestTable.baseTableRow
	}

}
