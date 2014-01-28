package pl.onewebpro.menu

import play.api.mvc.RequestHeader
import play.api.templates.{Html, PlayMagic}
import pl.onewebpro.secure.SecureService
import pl.onewebpro.session.SessionWrapper._

/**
 * @author loki
 */

/**
 * Menu item element
 * @param content Html Content of menu, example Html("Home")
 * @param routes Map[String,String] Map of routes, controller -> action.
 * @param link Html link to menu
 * @param elementArgs Map[Symbol,Any] Arguments for <li /> element
 * @param linkArgs Map[Symbol,Any] Arguments for <a /> element
 * @param show menu.ItemShow.Value When menu item should be visible
 * @param items Option(Vector[MenuItem]) Sub menu elements
 * @param itemType menu.ItemType.Value Type of menu element
 */
case class MenuItem(content: Html, routes: Set[(String, String)], link: Html = Html(""), elementArgs: Map[Symbol, Any] = Map(),
										linkArgs: Map[Symbol, Any] = Map(), show: ItemShow.Value = ItemShow.:*, items: Option[Vector[MenuItem]] = None,
										itemType: ItemType.Value = ItemType.Link)

/**
 * Menu element contains elements hidden if responsive and always show elements
 * @param hidden Option(Vector[MenuItem]) MenuItem elements hidden if menu bigger then size of screen
 * @param elements Option(Vector[MenuItem]) MenuItem elements always show
 */
case class Menu(hidden: Option[Vector[MenuItem]] = None, elements: Option[Vector[MenuItem]] = None)

/**
 * MenuItem type
 */
object ItemType extends Enumeration {
	val Link, Divider, Other = Value
}

/**
 * Element show type
 */
object ItemShow extends Enumeration {
	/**
	 * ':*' -> always
	 * ':@' -> logged
	 * ':?' -> guest
	 */
	val :*, :@, :? = Value
}

/**
 * Menu position
 */
object MenuPosition extends Enumeration {
	val FixedTop, FixedBottom, StaticTop = Value
}

/**
 * Bootstrap NavBar generator.
 * @param elements Menu A menu arguments
 * @param args  Map[Symbol, Any] Html arguments for <div class="navbar" />
 * @param name String A name of navbar
 * @param brand Option[String] Brand name, if None they will be no brand
 * @param link Html A @brand link
 * @param position menu.MenuPosition.Value Menu position
 * @param role String Role name of nav
 * @param inverse Boolean True to set black color of menu
 * @param responsive Boolean True to create responsive lines for menu
 * @param usePackage Boolean True to use a com.example.Controller or just Controller to check for items if they are active now
 * @param request RequestHeader Implicit request for get actual action and controller
 * @param secure SecureService Implicit secure service
 */
class Nav(elements: Menu, args: Map[Symbol, Any], name: String, brand: Option[String] = None, link: Html = Html("#"), position: MenuPosition.Value = MenuPosition.StaticTop,
					role: String = "navigation", inverse: Boolean = false, responsive: Boolean = true, usePackage: Boolean = true)
				 (implicit request: RequestHeader, secure: SecureService) {

	val action: String = s.getAction.getOrElse("")

	val controller: String = s.getController.getOrElse("")

	val controllerName: String = s.wrapController(s.getController)

	val magic = PlayMagic

	/**
	 * Method generate menu
	 * @return Html
	 */
	def apply: Html = {
		val menu: Html = elements.hidden match {
			case Some(_) => {
				elements.hidden.get.foldLeft[Html](Html("<ul class=\"nav navbar-nav\">")) {
					(body, element) => {
						if (checkElement(element))
							body += createElement(element, elements.hidden.get)
						else
							body += Html("")
					}
				} += Html("</ul>")
			}
			case _ => {
				Html("")
			}
		}

		views.html.onewebpro.nav.bar.render(getNavArgs, role, name, brand, link, responsive, menu)
	}

	/**
	 * Check if menu item is visible
	 * @param item MenuItem
	 * @return
	 */
	def checkElement(item: MenuItem): Boolean = item.show match {
		case ItemShow.:* => {
			true
		}
		case ItemShow.:@ => {
			secure.userIsLogged()
		}
		case ItemShow.:? => {
			!secure.userIsLogged()
		}
		case _ => {
			false
		}
	}

	/**
	 * Create menu class
	 * @return String
	 */
	private[menu] def navClass: String = {
		" navbar " + getNavPosition + getNavColor
	}

	/**
	 * Check nav color and return inverse color class if menu has inverse true
	 * @return String
	 */
	private[menu] def getNavColor: String = {
		if (inverse)
			" navbar-inverse"
		" navbar-default"
	}

	/**
	 * Return menu position class
	 * @return String
	 */
	private[menu] def getNavPosition: String = {
		position match {
			case MenuPosition.FixedTop => " navbar-fixed-top"
			case MenuPosition.FixedBottom => " navbar-fixed-bottom"
			case MenuPosition.StaticTop => " navbar-static-top"
			case _ => ""
		}
	}

	/**
	 * Prepare and return menu arguments
	 * @return Html
	 */
	private[menu] def getNavArgs: Html = {
		magic.toHtmlArgs(args.get('class) match {
			case Some(_) => {
				args.updated('class, (args.get('class).get.toString + navClass).trim)
			}
			case None => {
				args.+(('class, navClass.trim))
			}
		})
	}

	/**
	 * Prepare and return transform MenuItem to Html
	 * @param element MenuItem
	 * @return Html
	 */
	private[menu] def createElement(element: MenuItem, list: Vector[MenuItem]): Html = {
		element.itemType match {
			case ItemType.Divider => views.html.onewebpro.nav.divider.render()
			case ItemType.Other => element.content
			case _ => views.html.onewebpro.nav.element.render(elementArgs(element, list), elementLinkArgs(element), element.link, elementContent(element))
		}
	}

	/**
	 * Check element position in list
	 * @param item MenuItem
	 * @param vector Vector[MenuItem]
	 * @return String
	 */
	def elementPosition(item: MenuItem, vector: Vector[MenuItem]): String = {
		if (vector.head == item)
			return "first item-" + (vector.indexOf(item) + 1)
		else if (vector.last == item) {
			return "last item-" + (vector.indexOf(item) + 1)
		} else {
			return "item-" + (vector.indexOf(item) + 1)
		}
		""
	}

	/**
	 * Prepare and return @element arguments for <li>
	 * @param element MenuItem
	 * @return Html
	 */
	private[menu] def elementArgs(element: MenuItem, list: Vector[MenuItem]): Html = {
		magic.toHtmlArgs(args.get('class) match {
			case Some(_) => {
				args.updated('class, (args.get('class) + elementActive(element)).trim + " menuItem " + elementPosition(element, list).trim)
			}
			case None => {
				args.+(('class, elementActive(element).trim + " menuItem " + elementPosition(element, list).trim))
			}
		})
	}

	/**
	 * Check if @element is active
	 * @param element MenuItem
	 * @return String
	 */
	private[menu] def elementActive(element: MenuItem): String = {
		element.routes.foreach {
			case (c, a) => {
				if (usePackage) {
					if (c == controllerName && a == action) {
						return " active"
					}
				}
				if (c == controller && a == action) {
					return " active"
				}
			}
		}
		""
	}

	/**
	 * Return a args for <a /> of @element
	 * @param element MenuItem
	 * @return Html
	 */
	private[menu] def elementLinkArgs(element: MenuItem): Html = {
		PlayMagic.toHtmlArgs(element.linkArgs)
	}

	/**
	 * Create subMenu if @element has sub elements or return element content
	 * @param element MenuItem
	 * @return Html
	 */
	private[menu] def elementContent(element: MenuItem): Html = {
		element.items match {
			case Some(_) => {
				element.content += element.items.get.foldLeft[Html](Html("<ul class=\"dropdown-menu\">")) {
					(body, element) => body += createElement(element, element.items.get)
				} += Html("<b class=\"caret\"></b></ul>")
			}
			case _ => {
				element.content
			}
		}
	}
}
