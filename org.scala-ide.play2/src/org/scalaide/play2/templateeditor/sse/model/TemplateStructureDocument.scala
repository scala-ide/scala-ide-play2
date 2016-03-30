package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.jface.text.IDocumentExtension
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.IDocumentPartitioner
import org.eclipse.jface.text.IDocumentPartitioningListener
import org.eclipse.jface.text.IPositionUpdater
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.Position
import org.eclipse.wst.sse.core.internal.encoding.EncodingMemento
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser
import org.eclipse.wst.sse.core.internal.provisional.events.IModelAboutToBeChangedListener
import org.eclipse.wst.sse.core.internal.provisional.events.IStructuredDocumentListener
import org.eclipse.wst.sse.core.internal.provisional.events.StructuredDocumentEvent
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegionList
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredTextReParser
import org.eclipse.wst.sse.core.internal.text.JobSafeStructuredDocument
import org.eclipse.wst.sse.core.internal.undo.IStructuredTextUndoManager
import org.scalaide.play2.templateeditor.processing.TemplateVersionExhibitor

/**
 * It's extension of class returned by `StructuredDocumentFactory.getNewStructuredDocumentInstance`.
 * Done to keep info about template version (actually Play version) needed if Play Plugin wants to
 * support Play projects of different versions. `templateParserVersion` is set in `ThreadLocal` before
 * every public action and is used by `TemplateProcessingProvider` to choose correct Twirl parser
 * version to parse template document in editor.
 */
case class TemplateStructuredDocument(private val regionParser: RegionParser, templateParserVersion: Option[String] = None)
    extends JobSafeStructuredDocument(regionParser) {

  private def withParserVersion[T](f: => T) = {
    TemplateVersionExhibitor.set(templateParserVersion)
    f
  }

  // Members declared in org.eclipse.core.runtime.IAdaptable
  override def getAdapter[T](adapter: Class[T]): T = withParserVersion { super.getAdapter(adapter) }

  // Members declared in org.eclipse.jface.text.IDocument
  override def addDocumentListener(listener: IDocumentListener): Unit = withParserVersion { super.addDocumentListener(listener) }
  override def addDocumentPartitioningListener(listener: IDocumentPartitioningListener): Unit = withParserVersion { super.addDocumentPartitioningListener(listener) }
  override def addPosition(content: String, position: Position): Unit = withParserVersion { super.addPosition(content, position) }
  override def addPosition(position: Position): Unit = withParserVersion { super.addPosition(position) }
  override def addPositionCategory(cat: String): Unit = withParserVersion { super.addPositionCategory(cat) }
  override def addPositionUpdater(updater: IPositionUpdater): Unit = withParserVersion { super.addPositionUpdater(updater) }
  override def addPrenotifiedDocumentListener(listener: IDocumentListener): Unit = withParserVersion { super.addPrenotifiedDocumentListener(listener) }
  override def computeIndexInCategory(cat: String, offset: Int): Int = withParserVersion { super.computeIndexInCategory(cat, offset) }
  override def computeNumberOfLines(text: String): Int = withParserVersion { super.computeNumberOfLines(text) }
  override def computePartitioning(offset: Int, length: Int): Array[ITypedRegion] = withParserVersion { super.computePartitioning(offset, length) }
  override def containsPosition(category: String, offset: Int, length: Int): Boolean = withParserVersion { super.containsPosition(category, offset, length) }
  override def containsPositionCategory(category: String): Boolean = withParserVersion { super.containsPositionCategory(category) }
  override def get(offset: Int, length: Int): String = withParserVersion { super.get(offset, length) }
  override def get(): String = withParserVersion { super.get() }
  override def getChar(offset: Int): Char = withParserVersion { super.getChar(offset) }
  override def getContentType(offset: Int): String = withParserVersion { super.getContentType(offset) }
  override def getDocumentPartitioner(): IDocumentPartitioner = withParserVersion { super.getDocumentPartitioner }
  override def getLegalContentTypes(): Array[String] = withParserVersion { super.getLegalContentTypes }
  override def getLegalLineDelimiters(): Array[String] = withParserVersion { super.getLegalLineDelimiters }
  override def getLength(): Int = withParserVersion { super.getLength }
  override def getLineDelimiter(line: Int): String = withParserVersion { super.getLineDelimiter(line) }
  override def getLineInformation(line: Int): IRegion = withParserVersion { super.getLineInformation(line) }
  override def getLineInformationOfOffset(offset: Int): IRegion = withParserVersion { super.getLineInformationOfOffset(offset) }
  override def getLineLength(line: Int): Int = withParserVersion { super.getLineLength(line) }
  override def getLineOffset(line: Int): Int = withParserVersion { super.getLineOffset(line) }
  override def getNumberOfLines(offset: Int, length: Int): Int = withParserVersion { super.getNumberOfLines(offset, length) }
  override def getNumberOfLines(): Int = withParserVersion { super.getNumberOfLines }
  override def getPartition(offset: Int): ITypedRegion = withParserVersion { super.getPartition(offset) }
  override def getPositionCategories(): Array[String] = withParserVersion { super.getPositionCategories }
  override def getPositionUpdaters(): Array[IPositionUpdater] = withParserVersion { super.getPositionUpdaters }
  override def getPositions(category: String): Array[Position] = withParserVersion { super.getPositions(category) }
  override def insertPositionUpdater(updater: IPositionUpdater, index: Int): Unit = withParserVersion { super.insertPositionUpdater(updater, index) }
  override def removeDocumentListener(listener: IDocumentListener): Unit = withParserVersion { super.removeDocumentListener(listener) }
  override def removeDocumentPartitioningListener(listener: IDocumentPartitioningListener): Unit = withParserVersion { super.removeDocumentPartitioningListener(listener) }
  override def removePosition(category: String, position: Position): Unit = withParserVersion { super.removePosition(category, position) }
  override def removePosition(position: Position): Unit = withParserVersion { super.removePosition(position) }
  override def removePositionCategory(category: String): Unit = withParserVersion { super.removePositionCategory(category) }
  override def removePositionUpdater(updater: IPositionUpdater): Unit = withParserVersion { super.removePositionUpdater(updater) }
  override def removePrenotifiedDocumentListener(documentAdapter: IDocumentListener): Unit = withParserVersion { super.removePrenotifiedDocumentListener(documentAdapter) }
  override def replace(offset: Int, length: Int, text: String): Unit = withParserVersion { super.replace(offset, length, text) }
  override def search(startOffset: Int, findString: String, forwardSearch: Boolean, caseSensitive: Boolean, wholeWord: Boolean): Int = withParserVersion { super.search(startOffset, findString, forwardSearch, caseSensitive, wholeWord) }
  override def set(text: String): Unit = withParserVersion { super.set(text) }
  override def setDocumentPartitioner(partitioner: IDocumentPartitioner): Unit = withParserVersion { super.setDocumentPartitioner(partitioner) }

  // Members declared in org.eclipse.jface.text.IDocumentExtension
  override def registerPostNotificationReplace(listener: IDocumentListener, replace: IDocumentExtension.IReplace): Unit = withParserVersion { super.registerPostNotificationReplace(listener, replace) }
  override def resumePostNotificationProcessing(): Unit = withParserVersion { super.resumePostNotificationProcessing() }
  override def startSequentialRewrite(normalize: Boolean): Unit = withParserVersion { super.startSequentialRewrite(normalize) }
  override def stopPostNotificationProcessing(): Unit = withParserVersion { super.stopPostNotificationProcessing() }
  override def stopSequentialRewrite(): Unit = withParserVersion { super.stopSequentialRewrite() }

  // Members declared in org.eclipse.wst.sse.core.internal.provisional.document.IEncodedDocument
  override def getPreferredLineDelimiter(): String = withParserVersion { super.getPreferredLineDelimiter }
  override def setPreferredLineDelimiter(delim: String): Unit = withParserVersion { super.setPreferredLineDelimiter(delim) }

  // Members declared in org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
  override def addDocumentAboutToChangeListener(listener: IModelAboutToBeChangedListener): Unit = withParserVersion { super.addDocumentAboutToChangeListener(listener) }
  override def addDocumentChangedListener(listener: IStructuredDocumentListener): Unit = withParserVersion { super.addDocumentChangedListener(listener) }
  override def addDocumentChangingListener(listener: IStructuredDocumentListener): Unit = withParserVersion { super.addDocumentChangingListener(listener) }
  override def clearReadOnly(arg0: Int, arg1: Int): Unit = withParserVersion { super.clearReadOnly(arg0, arg1) }
  override def containsReadOnly(arg0: Int, arg1: Int): Boolean = withParserVersion { super.containsReadOnly(arg0, arg1) }
  override def getEncodingMemento(): EncodingMemento = withParserVersion { super.getEncodingMemento }
  override def getFirstStructuredDocumentRegion(): IStructuredDocumentRegion = withParserVersion { super.getFirstStructuredDocumentRegion }
  override def getLastStructuredDocumentRegion(): IStructuredDocumentRegion = withParserVersion { super.getLastStructuredDocumentRegion }
  override def getLineDelimiter(): String = withParserVersion { super.getLineDelimiter }
  override def getLineOfOffset(arg0: Int): Int = withParserVersion { super.getLineOfOffset(arg0) }
  override def getParser(): RegionParser = withParserVersion { super.getParser }
  override def getReParser(): IStructuredTextReParser = withParserVersion { super.getReParser }
  override def getRegionAtCharacterOffset(arg0: Int): IStructuredDocumentRegion = withParserVersion { super.getRegionAtCharacterOffset(arg0) }
  override def getRegionList(): IStructuredDocumentRegionList = withParserVersion { super.getRegionList }
  override def getStructuredDocumentRegions(): Array[IStructuredDocumentRegion] = withParserVersion { super.getStructuredDocumentRegions }
  override def getStructuredDocumentRegions(arg0: Int, arg1: Int): Array[IStructuredDocumentRegion] = withParserVersion { super.getStructuredDocumentRegions(arg0, arg1) }
  override def getText(): String = withParserVersion { super.getText }
  override def getUndoManager(): IStructuredTextUndoManager = withParserVersion { super.getUndoManager }
  override def makeReadOnly(arg0: Int, arg1: Int): Unit = withParserVersion { super.makeReadOnly(arg0, arg1) }
  override def newInstance(): IStructuredDocument = withParserVersion { super.newInstance() }
  override def removeDocumentAboutToChangeListener(arg0: IModelAboutToBeChangedListener): Unit = withParserVersion { super.removeDocumentAboutToChangeListener(arg0) }
  override def removeDocumentChangedListener(arg0: IStructuredDocumentListener): Unit = withParserVersion { super.removeDocumentChangedListener(arg0) }
  override def removeDocumentChangingListener(arg0: IStructuredDocumentListener): Unit = withParserVersion { super.removeDocumentChangingListener(arg0) }
  override def replaceText(arg0: Any, arg1: Int, arg2: Int, arg3: String, arg4: Boolean): StructuredDocumentEvent = withParserVersion { super.replaceText(arg0, arg1, arg2, arg3, arg4) }
  override def replaceText(arg0: Any, arg1: Int, arg2: Int, arg3: String): StructuredDocumentEvent = withParserVersion { super.replaceText(arg0, arg1, arg2, arg3) }
  override def setEncodingMemento(arg0: EncodingMemento): Unit = withParserVersion { super.setEncodingMemento(arg0) }
  override def setLineDelimiter(arg0: String): Unit = withParserVersion { super.setLineDelimiter(arg0) }
  override def setText(arg0: Any, arg1: String): StructuredDocumentEvent = withParserVersion { super.setText(arg0, arg1) }
  override def setUndoManager(arg0: IStructuredTextUndoManager): Unit = withParserVersion { super.setUndoManager(arg0) }
}
