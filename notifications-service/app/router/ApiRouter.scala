/*
 * Copyright (C) 2018  Joumen Ali HARZLI
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

package router

import javax.inject.Inject
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}
import web.NotificationController

/**
 * A Router for the [[web.NotificationController]]
 *
 * @author jaharzli
 */
class ApiRouter @Inject()(controller: NotificationController) extends SimpleRouter {

  override def routes: Router.Routes = {
    case POST(p"/notifications") => controller.add

    case PUT(p"/notifications/$userId/$creationDate/$notificationId/seen") =>
      controller.updateToSeen(userId, creationDate, notificationId)

    case GET(p"/notifications/$userid" ? q_o"page=$page") => controller.findByUserId(userid, page)
  }

}
