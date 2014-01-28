package pl.onewebpro.database

import play.api.db.slick.DB
import scala.slick.session.Session
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

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

  /**
   * Catch errors in service and inject database session
   * @param block
   * @tparam T
   * @return
   */
  def withError[T](block: (scala.slick.session.Session) => T): Either[ServiceError, T] = DB.withSession {
    implicit s: scala.slick.session.Session =>
      try {
        Right(block(s))
      } catch {
        case ex: ServiceException => Left(ServiceError(ex.getMessage))
      }
  }

  /**
   * Catch errors in service and inject database session with transaction
   * @param block
   * @tparam T
   * @return
   */
  def withErrorTransaction[T](block: (scala.slick.session.Session) => T): Either[ServiceError, T] = DB.withTransaction {
    implicit s: scala.slick.session.Session =>
      try {
        Right(block(s))
      } catch {
        case ex: ServiceException => Left(ServiceError(ex.getMessage))
      }
  }

  /**
   * Inject database session
   * @param block
   * @tparam T
   * @return
   */
  def withSession[T](block: (scala.slick.session.Session) => T): T = DB.withSession {
    implicit s: scala.slick.session.Session =>
      block(s)
  }

  /**
   * Inject database ssionn with transaction
   * @param block
   * @tparam T
   * @return
   */
  def withTransaction[T](block: (scala.slick.session.Session) => T): T = DB.withTransaction {
    implicit s: scala.slick.session.Session =>
      block(s)
  }

  /**
   * Inject database session
   * @param block
   * @tparam T
   * @return
   */
  def withSessionEither[T](block: (scala.slick.session.Session) => Either[ServiceError, T]): Either[ServiceError, T] = DB.withSession {
    implicit s: scala.slick.session.Session =>
      block(s)
  }

  /**
   * Inject database ssionn with transaction
   * @param block
   * @tparam T
   * @return
   */
  def withTransactionEither[T](block: (scala.slick.session.Session) => Either[ServiceError, T]): Either[ServiceError, T] = DB.withTransaction {
    implicit s: scala.slick.session.Session =>
      block(s)
  }
}

/**
 * Service error case class
 * @param error
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
}

/**
 * Table element with implementd some default database actions and fields
 * @param table
 * @tparam T
 */
abstract class Mapper[T <: Entity[T]](table: String) extends Table[T](None, table) {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def active = column[Boolean]("active")

  def autoInc = * returning id

  /**
   * Deleted object from database using id
   * @param id Long
   * @return
   */
  def delete(id: Long)(implicit s: Session): Boolean = {
    this.filter(_.id === id).delete > 0
  }

  /**
   * Query returning all elemnts using active field correct
   */
  lazy val findAllQuery = for {
    active <- Parameters[Boolean]
    e <- this if e.active === active
  } yield e

  /**
   * Use findAllQuery. Default is searching if filed approved is true
   * @param active Boolean
   * @return
   */
  def findAll(active: Boolean = true)(implicit s: Session): List[T] = {
    findAllQuery(active).list
  }

  /**
   * Query searching by id field
   */
  lazy val findByIdQuery = for {
    id <- Parameters[Long]
    e <- this if e.id === id && e.active === true
  } yield e

  /**
   * Searching element using id field. Return Option element
   * @param id Long
   * @return
   */
  def findById(id: Long)(implicit s: Session): Option[T] = {
    findByIdQuery(id).firstOption
  }

  /**
   * Insert entity element to database and return it. If element hase id defined nothing will happen.
   * @return
   */
  def insert(entity: T)(implicit s: Session): T = {
    if (!entity.id.isDefined) {
      val id = autoInc.insert(entity)
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
        this.filter(_.id === id).update(entity)
    }
    entity
  }

  /**
   * Update & Insert. If hase defined id it will updated if not it will be inserted.
   * @return
   */
  def upinsert(entity: T)(implicit s: Session): T = {
    entity.id.map {
      id =>
        this.filter(_.id === id).update(entity)
    }
    if (!entity.id.isDefined) {
      insert(entity)
    } else {
      entity
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
   * Method update entity if hase id
   * @return
   */
  def update(element: Element)(implicit session: Session): Element = self.update(element)

  /**
   * Update & Insert. If hase defined id it will updated if not it will be inserted.
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
   * Use findAllQuery. Default is searching if filed approved is true
   * @param active Boolean
   * @return
   */
  def findAll(active: Boolean = true)(implicit session: Session): List[Element] = self.findAll(active)
}