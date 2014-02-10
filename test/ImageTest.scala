import java.io.File
import org.specs2.mutable.Specification
import pl.onewebpro.image.{ImageResize, Image}

/**
 * @author loki
 */
class ImageTest extends Specification {

	val imageFile: File = new File("./test/files/image.jpg")
	"Image" should {
		"exists" in {
			imageFile.exists() mustEqual true
		}
	}

	"Image" should {
		"have good mime and be image" in {
			val image: Image = new Image(imageFile)
			image.isImage mustEqual true
			image.getExtensionName mustEqual "jpg"
			image.getFileMime.get mustEqual "image/jpeg"
		}
	}

}
