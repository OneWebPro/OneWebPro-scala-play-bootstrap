import pl.onewebpro.database.{Entity, DatabaseDAO}
import play.api.db.slick.Config.driver.simple._
import tables.TestComponent

/**
 * @author loki
 */
package object dao {

	private[dao] trait DAO[Element <: Entity[Element]] extends DatabaseDAO[Element] with DaoStructure

	trait DaoStructure extends TestComponent {
		val TestTable = TableQuery[TestTable]
	}

	object DDL extends DaoStructure {
		val ddl = TestTable.ddl
	}

	object TestTable extends DaoStructure {
		val self = TestTable.baseTableRow
	}

}
