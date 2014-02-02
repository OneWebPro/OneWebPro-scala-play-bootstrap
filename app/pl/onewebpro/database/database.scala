package pl.onewebpro.database

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import scala.slick.jdbc.JdbcBackend

/**
 * @author loki
 */

/**
 * Service object trait
 */
trait ErrorService {

	/**
	 * Database exception type. We should catch all exceptions, only
	 * that we created.
	 */
	type ServiceException = java.lang.Exception

	type Session = JdbcBackend.Session

	/**
	 * Catch errors in service and inject database session
	 * @return
	 */
	def withError[T](f: (Session) => T): Either[ServiceError, T] = DB.withSession {
		implicit session =>
			try {
				Right(f(session))
			} catch {
				case ex: ServiceException => Left(ServiceError(ex.getMessage))
			}
	}

	/**
	 * Catch errors in service and inject database session with transaction
	 * @return
	 */
	def withErrorTransaction[T](f: (Session) => T): Either[ServiceError, T] = DB.withTransaction {
		implicit session =>
			try {
				Right(f(session))
			} catch {
				case ex: ServiceException => Left(ServiceError(ex.getMessage))
			}
	}

	/**
	 * Inject database session
	 * @return
	 */
	def withSession[T](f: (Session) => T): T = DB.withSession {
		implicit session =>
			f(session)
	}

	/**
	 * Inject database ssionn with transaction
	 * @return
	 */
	def withTransaction[T](f: (Session) => T): T = DB.withTransaction {
		implicit session =>
			f(session)
	}

	/**
	 * Inject database session
	 * @return
	 */
	def withSessionEither[T](f: (Session) => Either[ServiceError, T]): Either[ServiceError, T] = DB.withSession {
		implicit session =>
			f(session)
	}

	/**
	 * Inject database ssionn with transaction
	 * @return
	 */
	def withTransactionEither[T](f: (Session) => Either[ServiceError, T]): Either[ServiceError, T] = DB.withTransaction {
		implicit session =>
			f(session)
	}
}

/**
 * Service error case class
 * @param error String
 */
case class ServiceError(error: String)

/**
 * Global table element
 */
trait Entity[T <: Entity[T]] {
	val id: Option[Long]
	val active: Boolean

	/**
	 * Method for copy object and replace its id
	 * @param id Long
	 * @return
	 */
	def withId(id: Long): T

	def deactivate: T
}

/**
 * Table element with implementd some default database actions and fields
 */
abstract class Mapper[T <: Entity[T]](table: String)(implicit val tag: Tag) extends Table[T](tag, table) {

	val self = TableQuery(tag => this)

	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

	def active = column[Boolean]("active")

	/**
	 * Deleted object from database using id
	 * @param id Long
	 * @return
	 */
	def delete(id: Long)(implicit s: Session): Boolean = {
		self.filter(_.id === id).delete > 0
	}

	/**
	 * Make element inactive
	 * @param id Long
	 * @return
	 */
	def remove(id: Long)(implicit s: Session): Boolean = !update(findById(id).get.deactivate).active

	/**
	 * Alias for remove
	 * @param id Long
	 * @return
	 */
	def deactivate(id: Long)(implicit s: Session): Boolean = remove(id)

	/**
	 * Query returning all elemnts using active field correct
	 */
	lazy val findActiveQuery = for {
		active <- Parameters[Boolean]
		e <- self if e.active === active
	} yield e

	/**
	 * Use findAllQuery. Default is searching if filed approved is true
	 * @param active Boolean
	 * @return
	 */
	def findActive(active: Boolean = true)(implicit s: Session): List[T] = {
		findActiveQuery(active).list
	}

	def findAll()(implicit s: Session): List[T] = (for {
		e <- self
	} yield e).list()

	/**
	 * Query searching by id field
	 **/
	lazy val findByIdQuery = for {
		id <- Parameters[Long]
		e <- self if e.id === id
	} yield e

	/**
	 * Searching element using id field.Return Option element
	 * @param id Long
	 * @return
	 **/
	def findById(id: Long)(implicit s: Session): Option[T] = {
		findByIdQuery(id).firstOption
	}

	/**
	 * Query searching by id field and active field
	 **/
	lazy val findByIdActiveQuery = for {
		(id, active) <- Parameters[(Long, Boolean)]
		e <- self if e.id === id && e.active === active
	} yield e

	/**
	 * Searching element using id field.Return Option element
	 * @param id Long
	 * @return
	 **/
	def findByIdActive(id: Long, active: Boolean = true)(implicit s: Session): Option[T] = {
		findByIdActiveQuery(id, active).firstOption
	}

	/**
	 * Insert entity element to database and return it.If element hase id defined nothing will happen.
	 * @return
	 */
	def insert(entity: T)(implicit s: Session): T = {
		if (!entity.id.isDefined) {
			val id = self returning self.map(_.id) += entity
			entity.withId(id)
		} else {
			entity
		}
	}

	/**
	 * Method update entity if hase id
	 * @return
	 */
	def update(entity: T)(implicit s: Session): T = {
		entity.id.map {
			id =>
				self.filter(_.id === id).update(entity)
		}
		entity
	}

	/**
	 * Update & Insert.If hase defined id it will updated if not it will be inserted.
	 * @return
	 */
	def upinsert(entity: T)(implicit s: Session): T = {
		entity.id.map {
			id =>
				self.filter(_.id === id).update(entity)
		} match {
			case Some(id: Int) =>
				entity.withId(id)
			case None =>
				insert(entity)
		}
	}

}

/**
 * DAO trait is trait that help implements all default methods from database.Mapper
 */
trait DatabaseDAO[Element <: Entity[Element]] {
	/**
	 * Element of DAO
	 */
	val self: Mapper[Element]

	/**
	 * Insert entity element to database and return it. If element had id defined nothing will happen.
	 * @return
	 */
	def insert(element: Element)(implicit session: Session): Element = self.insert(element)

	/**
	 * Method update entity if has id
	 * @return
	 */
	def update(element: Element)(implicit session: Session): Element = self.update(element)

	/**
	 * Update & Insert. If has defined id it will updated if not it will be inserted.
	 * @return
	 */
	def upinsert(element: Element)(implicit session: Session): Element = self.upinsert(element)

	/**
	 * Searching element using id field. Return Option element
	 * @param id Long
	 * @return
	 */
	def findById(id: Long)(implicit session: Session): Option[Element] = self.findById(id)

	/**
	 * Searching element using id field and active field
	 * @param id Long
	 * @param active Boolean
	 * @return
	 */
	def findByIdActive(id: Long, active: Boolean = true)(implicit session: Session): Option[Element] = self.findByIdActive(id, active)

	/**
	 * Use findAllQuery. Default is searching if filed approved is true
	 * @param active Boolean
	 * @return
	 */
	def findActive(active: Boolean = true)(implicit session: Session): List[Element] = self.findActive(active)

	/**
	 * Return all elements from database
	 * @return
	 */
	def findAll()(implicit session: Session): List[Element] = self.findAll()
}