package org.scalaide.play2.routeeditor.handlers

import org.junit.Test
import org.junit.Assert._
import org.scalaide.play2.routeeditor.RouteTest
import org.eclipse.jface.text.TextSelection

/**
 * Test the computation of the matching location to be edited during local rename in the route editor.
 * '@' is used to mark the caret position (only 1), or the text selection (2 of them).
 * '%' is used to mark the regions which are expected.
 */
class LocalRenameTest extends RouteTest {

  @Test
  def replaceSingleLocationOnInvalidPath {
    testLocations("GET %pa@th%")
  }

  @Test
  def replaceLocationsOnInvalidPaths {
    testLocations(
      """GET %path%
        |GET %pa@th%/abc""")
  }
  
  @Test
  def replaceLocationsOnValidAndInvalidPaths {
    testLocations(
      """GET %path%
        |GET /%pa@th%""")
  }

  @Test
  def replaceLocationsNoSelection {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%a@bc%/gh   controller
          |GET /path/%abc%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsSelectionInOnePart {
    testLocations(
      """GET /path/abc/def  controller
          |GET /path/abc/gh   controller
          |GET /path/abc      controller
          |GET /path/abc/     controller
          |GET /path/%a@bc@d%/efg controller
          |GET /path/%abcd%     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsSelectionEndsBeforeSlash {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%a@bc@%/gh   controller
          |GET /path/%abc%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsSelectionStartsAfterSlash {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%@ab@c%/gh   controller
          |GET /path/%abc%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsSelectionOverToParts {
    testLocations(
      """GET /%path/abc%/def  controller
          |GET /%path/abc%/gh   controller
          |GET /%pa@th/ab@c%      controller
          |GET /%path/abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsCaretInPart {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%abc%/gh   controller
          |GET /path/%ab@c%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsCaretAfterSlash {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%@abc%/gh   controller
          |GET /path/%abc%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def replaceLocationsCaretBeforeSlash {
    testLocations(
      """GET /path/%abc%/def  controller
          |GET /path/%abc@%/gh   controller
          |GET /path/%abc%      controller
          |GET /path/%abc%/     controller
          |GET /path/abcd/efg controller
          |GET /path/abcd     controller
          |GET /path/ab       controller""")
  }

  @Test
  def caretInAction {
    testLocations("GET /path/abc/def contr@oller")
  }

  @Test
  def selectionInAction {
    testLocations("GET /path/abc/def contr@oll@er")
  }

  @Test
  def caretInMethod {
    testLocations("G@ET /path/abc/def controller")
  }

  @Test
  def selectionInMethod {
    testLocations("G@E@T /path/abc/def controller")
  }

  @Test
  def selectionPartInMethod {
    testLocations("GE@T  /path/a@bc/def  controller")
  }

  @Test
  def selectionPartInAction {
    testLocations("GET  /path/ab@c/def  contr@oller")
  }

  @Test
  def selectionStartsInMethodEndsInAction {
    testLocations("G@ET /path/abc/def contr@oller")
  }

  @Test
  def caretInLeadingWhitespace {
    testLocations("GET @ /path/abc/def controller")
  }

  @Test
  def caretInTrailingWhitespace {
    testLocations("GET  /path/adc/def @ controller")
  }

  @Test
  def selectionPartInLeadingWhitespace {
    testLocations("GET @ /%path/a@bc%/  controller")
  }

  @Test
  def selectionPartInTrailingWhitespace {
    testLocations("GET  /path/%ab@c/def% @ controller")
  }

  @Test
  def selectionStartsAndEndsInWhitespace {
    testLocations("GET  @ /path/abc/def @ controller")
  }
  
  @Test
  def selectionStartAtLeadingSlash {
    testLocations("GET  @/%pa@th%/abc/def  controller")
  }

  @Test
  def selectionStartAfterLeadingSlash {
    testLocations("GET /%@pat@h%/abc/def controller")
  }
  
  @Test
  def selectionEndsAfterLeadingSlash {
    testLocations("GET @ /@%path%/abc/def controller")
  }
  
  @Test
  def caretAtLeadingSlash {
    testLocations("GET  @/path/abc/def controller")
  }
  
  @Test
  def caretAfterLeadingSlash {
    testLocations("GET /@%path%/abc/def controller")
  }
  
  @Test
  def selectionStartAtTrailingSlash {
    testLocations("GET /path/abc/def@/ @ controller")
  }
  
  @Test
  def selectionStartAfterTrailingSlash {
    testLocations("GET /path/abc/def/@  @ controller")
  }
  
  @Test
  def selectionEndsAfterTrailingSlash {
    testLocations("""GET /path/%abc%/def controller
        |GET /path/%a@bc%/@ controller
        |GET /path/%abc% controller""")
  }
  
  @Test
  def caretAtTrailingSlash {
    testLocations("GET /path/%abc%@/ controller")
  }
  
  @Test
  def caretAfterTrailingSlash {
    testLocations("GET /path/abc/@ controller")
  }
  
  @Test
  def caretAtRootPath {
    testLocations("GET @/ controller")
  }
  
  @Test
  def caretAfterRootPath {
    testLocations("GET /@ controller")
  }
  
  @Test
  def selectionAroundRootPath {
    testLocations("GET @/@ controller")
  }
  
  private def testLocations(rawContent: String) {
    val handler = new LocalRename()
    val file = RouteFile(rawContent, List('@', '%'))
    val selectedRegion = file.selectedRegion()

    // the region containing the selected area should be first
    val expected = file.selectedRegions('%').map(r => (r.getOffset, r.getLength()))
      .sortWith((a, b) => a._1 <= selectedRegion.getOffset() && selectedRegion.getOffset() <= a._1 + a._2)
      
    val expectedOption = if (expected.isEmpty) {
      None
    } else {
      Some(expected)
    }

    val actual = handler.matchingURIRegions(file.document, new TextSelection(file.document, selectedRegion.getOffset(), selectedRegion.getLength()))

    assertEquals("Unexpected Regions", expectedOption, actual)
  }

}