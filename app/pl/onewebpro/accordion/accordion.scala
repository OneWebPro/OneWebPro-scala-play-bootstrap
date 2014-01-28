package pl.onewebpro.accordion

import play.api.templates.{PlayMagic, Html}

/**
 * @author loki
 */

case class AccordionElement(link: Html, content: Html, name: String, collapse: Boolean = false)

/**
 * Collapse element
 * @param args Map[Symbol, Any]
 * @param accords Vector[AccordionElement]
 */
class Collapse(args: Map[Symbol, Any], accords: Vector[AccordionElement], name: String = "accordion") {

	val magic = PlayMagic

	val className: String = "panel-group"

	/**
	 * Render bootstrap accordion element
	 * @return
	 */
	def apply: Html = {
		views.html.onewebpro.accord.body(getAccordArgs, accords.map((e) => views.html.onewebpro.accord.elements(e.link, e.content, name + "-acc", e.name, e.collapse)))
	}

	/**
	 * Prepare and return menu arguments
	 * @return Html
	 */
	private def getAccordArgs: Html = {
		val arg = args.get('class) match {
			case Some(_) => {
				args.updated('class, (args.get('class).get.toString + " " + className).trim)
			}
			case None => {
				args.+(('class, className.trim))
			}
		}
		magic.toHtmlArgs(arg.get('id) match {
			case Some(_) => {
				args.updated('id, (args.get('id).get.toString + name + "-acc").trim)
			}
			case None => {
				args.+(('id, name + "-acc".trim))
			}
		})
	}


}
