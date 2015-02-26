package pl.onewebpro.image

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import net.coobird.thumbnailator.Thumbnails
import org.imgscalr.Scalr.Rotation
import org.imgscalr.{AsyncScalr, Scalr}
import play.api.libs.MimeTypes
import java.nio.file.{StandardCopyOption, Files}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}

/**
 * @author loki
 */
class Image(imageFile: File, defaultMime: List[String] = List("image/jpeg", "image/png", "image/bmp")) {

  val mimeTypes: List[String] = defaultMime

  var image: BufferedImage = ImageIO.read(imageFile)

  def getFileMime: Option[String] = MimeTypes.forFileName(imageFile.getName)

  def getFile: File = imageFile

  def isImage: Boolean = getFileMime match {
    case Some(mime: String) => mimeTypes.contains(mime)
    case _ => false
  }

  def copy(file: File): Image = {
    Files.createDirectories(file.getParentFile.toPath)
    ImageIO.write(image, getExtensionName, file)
    new Image(file, mimeTypes)
  }

  def move(file: File): Image = {
    Files.createDirectories(file.getParentFile.toPath)
    Files.copy(imageFile.toPath, file.toPath, StandardCopyOption.REPLACE_EXISTING)
    new Image(file, mimeTypes)
  }

  def remove: Boolean = getFile.delete()

  def width: Float = image.getWidth

  def height: Float = image.getHeight

  def getExtension: Option[String] = imageFile.getName.split('.').takeRight(1).headOption

  def getExtensionName: String = getExtension.get

  def toBytes()(implicit codec: Codec = Codec.ISO8859): Array[Byte] = Source.fromFile(imageFile).map(_.toByte).toArray

  //* Scalr methods *//

  def resize(targetWidth: Int, targetHeight: Int, quality: Double): Image = {
    Thumbnails.of(imageFile).size(targetWidth, targetHeight).outputQuality(quality).toFile(imageFile)
    new Image(imageFile, mimeTypes)
  }

  def resize(img: ImageResize): Image = resize(img.targetWidth, img.targetHeight, img.quality)

  def resizeAsync(targetWidth: Int, targetHeight: Int, quality: Double)(implicit ctx: ExecutionContext): Future[Image] = Future.apply {
    Thumbnails.of(imageFile).size(targetWidth, targetHeight).outputQuality(quality).toFile(imageFile)
    new Image(imageFile, defaultMime)
  }

  def resizeAsync(img: ImageResize)(implicit ctx: ExecutionContext): Future[Image] = resizeAsync(img.targetWidth, img.targetHeight, img.quality)

  def crop(x: Int, y: Int, width: Int, height: Int): Image = {
    ImageIO.write(Scalr.crop(image, x, y, width, height), getExtensionName, imageFile)
    new Image(imageFile, mimeTypes)
  }

  def crop(img: ImageCrop): Image = crop(img.x, img.y, img.width, img.height)

  def cropAsync(x: Int, y: Int, width: Int, height: Int)(implicit ctx: ExecutionContext): Future[Image] = Future.apply {
    ImageIO.write(AsyncScalr.crop(image, x, y, width, height).get(), getExtensionName, imageFile)
    new Image(imageFile, mimeTypes)
  }

  def cropAsync(img: ImageCrop)(implicit ctx: ExecutionContext): Future[Image] = cropAsync(img.x, img.y, img.width, img.height)

  def pad(padding: Int, color: Color = Color.BLACK): Image = {
    ImageIO.write(Scalr.pad(image, padding, color), getExtensionName, imageFile)
    new Image(imageFile, mimeTypes)
  }

  def pad(img: ImagePad): Image = pad(img.padding, img.color)

  def padAsync(padding: Int, color: Color = Color.BLACK)(implicit ctx: ExecutionContext): Future[Image] = Future.apply {
    ImageIO.write(AsyncScalr.pad(image, padding, color).get(), getExtensionName, imageFile)
    new Image(imageFile, defaultMime)
  }

  def padAsync(img: ImagePad)(implicit ctx: ExecutionContext): Future[Image] = padAsync(img.padding, img.color)

  def rotate(rotation: Rotation): Image = {
    ImageIO.write(Scalr.rotate(image, rotation), getExtensionName, imageFile)
    new Image(imageFile, mimeTypes)
  }

  def rotate(img: ImageRotate): Image = rotate(img.rotation)

  def rotateAsync(rotation: Rotation)(implicit ctx: ExecutionContext): Future[Image] =
    Future.apply {
      ImageIO.write(AsyncScalr.rotate(image, rotation).get(), getExtensionName, imageFile)
      new Image(imageFile, defaultMime)
    }

  def rotateAsync(img: ImageRotate)(implicit ctx: ExecutionContext): Future[Image] = rotateAsync(img.rotation)

  if (!getExtension.isDefined) throw new Exception("File" + imageFile.getName + "don't have file extension.")
  if (!isImage) throw new Exception("File" + imageFile.getName + "is not image compatible with mime types.")
}

case class ImageResize(targetWidth: Int, targetHeight: Int, quality: Double = 1.0)

case class ImageCrop(x: Int, y: Int, width: Int, height: Int)

case class ImagePad(padding: Int, color: Color = Color.BLACK)

case class ImageRotate(rotation: Rotation)
