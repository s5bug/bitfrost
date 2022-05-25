package ai.dragonfly.bitfrost.color.model.rgb.discrete

import ai.dragonfly.bitfrost.*
import ai.dragonfly.bitfrost.cie.WorkingSpace
import ai.dragonfly.bitfrost.color.model.*
import ai.dragonfly.math.{Random, squareInPlace}

import scala.language.implicitConversions

trait ARGB64 extends DiscreteRGB { self: WorkingSpace =>

  given Conversion[Long, ARGB64] with
    def apply(argb: Long): ARGB64 = ARGB64(argb)

  given Conversion[ARGB64, Long] with
    def apply(c: ARGB64): Long = c.argb

  object ARGB64 extends UtilDiscreteRGB64[ARGB64] {
    def apply(argb: Long): ARGB64 = new ARGB64(argb)

    /**
     * Factory method to create a fully opaque ARGB64 instance from separate, specified red, green, blue components and
     * a default alpha value of 65535.
     * Parameter values are derived from the least significant byte.  Integer values that range outside of [0-65535] may
     * give unexpected results.  For values taken from user input, sensors, or otherwise uncertain sources, consider using
     * the factory method in the Color companion object.
     *
     * @see [[ai.dragonfly.color.ColorVectorSpace.argb]] for a method of constructing ARGB64 objects that validates inputs.
     * @param red   integer value from [0-65535] representing the red component in RGB space.
     * @param green integer value from [0-65535] representing the green component in RGB space.
     * @param blue  integer value from [0-65535] representing the blue component in RGB space.
     * @return an instance of the ARGB64 case class.
     * @example {{{ val c = ARGB(72,105,183) }}}
     */
    def apply(red: Int, green: Int, blue: Int): ARGB64 = apply(MAX, red, green, blue)


    /**
     * Factory method to create an ARGB64 instance from separate, specified red, green, blue, and alpha components.
     * Parameter values are derived from the least significant byte.  Integer values that range outside of [0-65535] may
     * give unexpected results.  For values taken from user input, sensors, or otherwise uncertain sources, consider using
     * the factory method in the Color companion object.
     *
     * @see [[ai.dragonfly.color.ARGB64.getIfValid]] for a method of constructing ARGB64 objects with input validation.
     * @param alpha integer value from [0-65535] representing the alpha component in ARGB64 space.  Defaults to 65535.
     * @param red   integer value from [0-65535] representing the red component in RGB space.
     * @param green integer value from [0-65535] representing the green component in RGB space.
     * @param blue  integer value from [0-65535] representing the blue component in RGB space.
     * @return an instance of the ARGB64 case class.
     * @example {{{ val c = ARGB(72,105,183) }}}
     */
    def apply(alpha: Int, red: Int, green: Int, blue: Int): ARGB64 = apply((alpha << 48) | (red << 32) | (green << 16) | blue)

    /**
     * Factory method to create a fully Opaque ARGB64 color; one with an alpha value of 65535.
     * Because this method validates each intensity, it sacrifices performance
     * for safety.  Although well suited for parsing color data generated by sensors or user input, this method undercuts
     * performance in applications like reading image data.
     *
     * To skip validation and minimize overhead, @see [[ai.dragonfly.color.ARGB64.apply]]
     *
     * @param red   integer value from [0-65535] representing the red component in RGB space.
     * @param green integer value from [0-65535] representing the green component in RGB space.
     * @param blue  integer value from [0-65535] representing the blue component in RGB space.
     * @return an instance of the ARGB64 class or None if fed invalid input.
     */
    def getIfValid(red: Int, green: Int, blue: Int): Option[ARGB64] = getIfValid(MAX, red, green, blue)

    /**
     * Factory method to create an ARGB64 color.  Because this method validates each intensity, it sacrifices performance
     * for safety.  Although well suited for parsing color data generated by sensors or user input, this method undercuts
     * performance in applications like reading image data.
     *
     * To skip validation and minimize overhead, @see [[ai.dragonfly.color.ARGB64.apply]]
     *
     * @param alpha integer value from [0-65535] representing the alpha component in ARGB64 space.
     * @param red   integer value from [0-65535] representing the red component in RGB space.
     * @param green integer value from [0-65535] representing the green component in RGB space.
     * @param blue  integer value from [0-65535] representing the blue component in RGB space.
     * @return an instance of the C class or None if fed invalid input.
     */
    def getIfValid(alpha: Int, red: Int, green: Int, blue: Int): Option[ARGB64] = {
      if (valid(alpha, red, green, blue)) Some(apply(alpha, red, green, blue))
      else None
    }

    inline def clamp(red: Double, green: Double, blue: Double): Long = clamp(MAX, red, green, blue)

    override def fromXYZ(xyz: XYZ): ARGB64 = fromRGB(xyz.toRGB)

    override def fromRGB(rgb: RGB): ARGB64 = clamp(rgb.red * MAX, rgb.green * MAX, rgb.blue * MAX)

    override def weightedAverage(c1: ARGB64, w1: Double, c2: ARGB64, w2: Double): ARGB64 = ARGB64(
      (((c1.alpha * w1) + (c2.alpha * w2)) / 2.0).toInt,
      (((c1.red * w1) + (c2.red * w2)) / 2.0).toInt,
      (((c1.green * w1) + (c2.green * w2)) / 2.0).toInt,
      (((c1.blue * w1) + (c2.blue * w2)) / 2.0).toInt
    )

    /**
     * Use Color.random() to obtain a random color in the form of an ARGB64 instance.
     * This method executes quickly and without memory costs, but the RGB color space biases toward cool colors.
     * In contrast, the Color.randomFromLabSpace() method takes seconds to initialize and has a memory footprint of several megabytes
     * However, it samples from a perceptually uniform color space and avoids the bias toward cool colors.
     * This method samples the Red, Green, and Blue color components uniformly, but always returns 65535 for the alpha component.
     *
     * @return a randomly generated color sampled from the RGB Color Space.
     */
    override def random(r: scala.util.Random = Random.defaultRandom): ARGB64 = 0xFFFF000000000000L | r.nextLong(0xFFFFFFFFFFFFL)
  }


  /**
   * ARGB64 is the primary case class for representing colors in ARGB64 space.
   *
   * @constructor Create a new ARGB64 object from an Int.
   * @see [[https://en.wikipedia.org/wiki/RGB_color_space]] for more information on the RGB color space.
   * @param argb a 64 bit integer that represents this color in ARGB64 space.
   *             The most significant byte encodes the alpha value, the second most significant byte encodes red,
   *             the third most significant byte encodes green, and the least significant byte encodes blue.
   * @return an instance of the ARGB64 case class.
   * @example {{{
   * val c = ARGB(-1)  // returns fully opaque white
   * c.toString()  // returns "ARGB(65535,65535,65535,65535)"
   * ARGB(0xFF0000FF).toString() // returns "ARGB(65535,0,0,65535)"
   * }}}
   */
  case class ARGB64(argb: Long) extends DiscreteRGB[ARGB64] {
    /**
     * @return the alpha component of this color in ARGB64 space.
     */
    inline def alpha: Int = (argb >> 48 & 0xFFFFL).toInt

    /**
     * @return the red component of this color in ARGB64 space.
     */
    inline def red: Int = (argb >> 32 & 0xFFFFL).toInt

    /**
     * @return the green component of this color in ARGB64 space.
     */
    inline def green: Int = (argb >> 16 & 0xFFFFL).toInt

    /**
     * @return the blue component of this color in ARGB64 space.
     */
    inline def blue: Int = (argb & 0xFFFFL).toInt

    override def toRGB: RGB = {
      import ARGB64.MAXD
      RGB(red.toDouble / MAXD, green.toDouble / MAXD, blue.toDouble / MAXD)
    }

    override def similarity(that: ARGB64): Double = Math.sqrt(
      squareInPlace(red - that.red) +
        squareInPlace(green - that.green) +
        squareInPlace(blue - that.blue)
    )

    /**
     * @return true if these colors are equal in ARGB64 space, false otherwise
     */
    override def equals(obj: Any): Boolean = obj match {
      case that: ARGB64 => this.argb == that.argb
      case _ => false
    }

    /**
     * @return a hexadecimal string representing the rgba integer for this color.
     * @example {{{
     * val c = ARGB64(72,105,183)
     * c.hex() // returns "ff4869b7"
     * }}}
     */
    def hex(): String = java.lang.Long.toHexString(argb)

    override def toString: String = s"ARGB64($alpha, $red, $green, $blue)"
  }

}
