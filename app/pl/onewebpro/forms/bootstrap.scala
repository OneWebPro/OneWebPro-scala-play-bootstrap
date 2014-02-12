package pl.onewebpro.forms

import views.html.helper.FieldConstructor

/**
 * @author loki
 */

object Forms {
	implicit val horizontalInput = FieldConstructor(views.html.onewebpro.horizontalInput.f)
	implicit val verticalInput = FieldConstructor(views.html.onewebpro.verticalInput.f)

	implicit val horizontalRadio = FieldConstructor(views.html.onewebpro.horizontalRadio.f)
	implicit val verticalRadio = FieldConstructor(views.html.onewebpro.verticalRadio.f)

	implicit val horizontalCheckbox = FieldConstructor(views.html.onewebpro.horizontalCheckbox.f)
	implicit val verticalCheckbox = FieldConstructor(views.html.onewebpro.verticalCheckbox.f)

	implicit val horizontalSelect = FieldConstructor(views.html.onewebpro.horizontalSelect.f)
	implicit val verticalSelect = FieldConstructor(views.html.onewebpro.verticalSelect.f)

}

