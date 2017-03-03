/*
 * Copyright (C) 2017  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.google.inject.ImplementedBy
import org.joda.time.LocalDateTime
import play.api.libs.json.{Reads, Writes}
import slicks.modules.SessionTable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case class SessionId(id: String)

@ImplementedBy(classOf[SessionTable])
trait SessionService {

  /**
    * Retrieve the entire session data associated with the `sessionId` and attempt to convert it to
    * a value of type `T`
    */
  def get[T: Reads](sessionId: SessionId): Future[Option[T]]

  /**
    * Retrieve a sub-section of the session data corresponding to the `key` and attempt
    * to convert it to a value of type `T`
    */
  def get[T: Reads](sessionId: SessionId, key: String): Future[Option[T]]

  /**
    * Accept a value of type `T` and store it into the session, associated with the
    * given `key`. Any previous value associated with `key` will be replaced.
    */
  def put[T: Writes](sessionId: SessionId, key: String, value: T): Future[Unit]

  /**
    * Remove any value associated with the given `key` from the session.
    */
  def clear(sessionId: SessionId, key: String): Future[Unit]

  /**
    * Refresh the expiry time of the session to be the current time plus the
    * timeout in minutes
    */
  def refresh(sessionId: SessionId, lifetimeInMinutes: Int = 20, now: LocalDateTime = LocalDateTime.now): Future[Unit]

  /**
    * Find any expired sessions (i.e. sessions that have expiry times that are earlier
    * than the current time) and remove them from the session store.
    */
  def removeExpired(now: LocalDateTime = LocalDateTime.now): Future[Unit]

}

/**
  * Schedule a process that will call the `SessionService.removeExpired` method periodically
  * to ensure that stale sessions are cleaned up.
  */
@Singleton
class SessionCleaner @Inject()(sessionService: SessionService, system: ActorSystem)(implicit ec: ExecutionContext) {
  system.scheduler.schedule(10 seconds, 30 seconds)(sessionService.removeExpired())
}