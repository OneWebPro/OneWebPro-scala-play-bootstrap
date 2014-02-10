package services

import pl.onewebpro.database.{ErrorService, ServiceError}
import tables._
import dao._
import scalaz._
import Scalaz._

/**
 * @author loki
 */
object TestService extends ErrorService {

	def findAllWithError(): Either[ServiceError, List[Test]] = withError {
		implicit session =>
			val list = TestTable.findAll()
			list.isEmpty match {
				case true => throw new ServiceException("List empty")
				case _ => list
			}
	}

	def findAllWithEither(): Either[ServiceError, List[Test]] = withSessionEither {
		implicit session =>
			val list = TestTable.findAll()
			list.isEmpty match {
				case true => Left(ServiceError("List empty"))
				case _ => Right(list)
			}
	}

	def findAllWithValidation(): Either[ServiceError, List[Test]] = withValidation {
		implicit session =>
			val list = TestTable.findAll()
			list.isEmpty match {
				case true => ServiceError("List empty").fail
				case _ => list.success
			}
	}
}
