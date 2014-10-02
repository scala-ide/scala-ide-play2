package org.scalaide.play2.routeeditor.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.handlers.HandlerUtil
import org.scalaide.play2.routeeditor.RouteEditor
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.text.ITextSelection
import org.scalaide.play2.routeeditor.RouteUri
import org.scalaide.play2.routeeditor.RouteUriWithRegion
import org.eclipse.ui.ISelectionService
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITypedRegion
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.util.eclipse.EditorUtils

class LocalRename extends AbstractHandler {

  override def execute(event: ExecutionEvent): AnyRef = {
    for {
      editor <- getRouteEditor(event)
      selection <- getTextSelection(editor)
      regions <- matchingURIRegions(editor.getViewer.getDocument(), selection)
    } {
        EditorUtils.enterLinkedModeUi(regions, false)
    }

    // always return null, as speced
    null
  }

  /** Returns the editor in which the action was triggered */
  private def getRouteEditor(event: ExecutionEvent): Option[RouteEditor] =
    HandlerUtil.getActiveEditor(event) match {
      case editor: RouteEditor =>
        Some(editor)
      case _ =>
        None
    }

  /** Returns the current selection in the given editor */
  private def getTextSelection(editor: RouteEditor): Option[ITextSelection] = {
    // not using HandlerUtil.getCurrentSelection(), because the returned value is not correctly updated
    // if the user just move the caret.

    editor.getSelectionProvider().getSelection() match {
      case textSelection: ITextSelection =>
        Some(textSelection)
      case _ =>
        None
    }
  }

  /** Returns tuples of (offset, length) of the URI parts in the document which are equivalent to the ones
   *  covered by the selection.
   */
  def matchingURIRegions(document: IDocument, selection: ITextSelection): Option[List[(Int, Int)]] = {
    findSelectedPartition(document, selection).flatMap {
      partition =>
        val baseUri = RouteUri(document.get(partition.getOffset(), partition.getLength()))

        val (prefixParts, renamedParts) = baseUri.partsTouchedBy(selection.getOffset() - partition.getOffset(), selection.getLength())

        if (renamedParts.isEmpty) {
          None
        } else {
          val fullPrefix = prefixParts ++ renamedParts

          // URIs with the same starting path
          val matchingUris = RouteUriWithRegion.allUrisInDocument(document).filter(p => p.startsWith(fullPrefix))

          if (matchingUris.isEmpty) {
            None
          } else {
            // put the URI containing the selected partition first
            val sortedMatchingUris = matchingUris.sortWith {
              (a, b) =>
                a.region.getOffset() == partition.getOffset()
            }

            // transform in tuples
            Some(sortedMatchingUris.map(p => {
              // prefixLength will include an ending '/' if the prefix is indeed a prefix (and not the whole uri).
              // However, when computing fullPrefix, this extra '/' is not wanted
              //  (for example, imagine uri = "/foo/bar", prefixParts=Nil, fullPrefix=List("foo"). In this case we
              //   want to highlight only "foo", NOT "foo/"
              val lengthAdjustment = if (fullPrefix == p.parts) 0 else 1
              val baseOffset = p.prefixLength(prefixParts)
              val baseLength = p.prefixLength(fullPrefix) - baseOffset - lengthAdjustment
              (p.region.getOffset() + baseOffset, baseLength)
            }))
          }
        }
    }
  }

  /** Returns the URI partition which contains the given selection. Returns None if it is not able to find a unique URI partition. */
  def findSelectedPartition(document: IDocument, selection: ITextSelection): Option[ITypedRegion] = {
    val selectionStart = selection.getOffset()
    val selectionEnd = selectionStart + selection.getLength()

    val partitionStart = document.getPartition(selectionStart)
    val partitionEnd = document.getPartition(selectionEnd)

    if (partitionStart == partitionEnd) {
      if (partitionStart.getType() == RoutePartitions.ROUTE_URI) {
        Some(partitionStart)
      } else {
        // only one partition, but not a route partition
        None
      }
    } else {
      if (partitionStart.getOffset() + partitionStart.getLength() != partitionEnd.getOffset()) {
        // partitions are not contiguous
        None
      } else {
        if (partitionStart.getType() == RoutePartitions.ROUTE_URI) {
          Some(partitionStart)
        } else if (partitionEnd.getType() == RoutePartitions.ROUTE_URI) {
          Some(partitionEnd)
        } else {
          // none of the partition are route partitions
          None
        }
      }
    }

  }

}

