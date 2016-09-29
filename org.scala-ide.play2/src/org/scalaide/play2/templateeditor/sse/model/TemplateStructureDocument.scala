package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.text.IDocument

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
import org.eclipse.wst.sse.core.internal.provisional.document.IEncodedDocument
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
    extends IAdaptable with IDocument with IDocumentExtension with IEncodedDocument with IStructuredDocument {
  private val delegateDoc = new JobSafeStructuredDocument(regionParser)
  def delegate = delegateDoc

  private def withParserVersion[T](f: => T) = {
    TemplateVersionExhibitor.set(templateParserVersion)
    f
  }

  // Members declared in org.eclipse.core.runtime.IAdaptable
  override def getAdapter[T](adapter: Class[T]): T = withParserVersion { delegateDoc.getAdapter(adapter) }

  // Members declared in org.eclipse.jface.text.IDocument
  override def addDocumentListener(listener: IDocumentListener): Unit = withParserVersion { delegateDoc.addDocumentListener(listener) }
  override def addDocumentPartitioningListener(listener: IDocumentPartitioningListener): Unit = withParserVersion { delegateDoc.addDocumentPartitioningListener(listener) }
  override def addPosition(content: String, position: Position): Unit = withParserVersion { delegateDoc.addPosition(content, position) }
  override def addPosition(position: Position): Unit = withParserVersion { delegateDoc.addPosition(position) }
  override def addPositionCategory(cat: String): Unit = withParserVersion { delegateDoc.addPositionCategory(cat) }
  override def addPositionUpdater(updater: IPositionUpdater): Unit = withParserVersion { delegateDoc.addPositionUpdater(updater) }
  override def addPrenotifiedDocumentListener(listener: IDocumentListener): Unit = withParserVersion { delegateDoc.addPrenotifiedDocumentListener(listener) }
  override def computeIndexInCategory(cat: String, offset: Int): Int = withParserVersion { delegateDoc.computeIndexInCategory(cat, offset) }
  override def computeNumberOfLines(text: String): Int = withParserVersion { delegateDoc.computeNumberOfLines(text) }
  override def computePartitioning(offset: Int, length: Int): Array[ITypedRegion] = withParserVersion { delegateDoc.computePartitioning(offset, length) }
  override def containsPosition(category: String, offset: Int, length: Int): Boolean = withParserVersion { delegateDoc.containsPosition(category, offset, length) }
  override def containsPositionCategory(category: String): Boolean = withParserVersion { delegateDoc.containsPositionCategory(category) }
  override def get(offset: Int, length: Int): String = withParserVersion { delegateDoc.get(offset, length) }
  override def get(): String = withParserVersion { delegateDoc.get() }
  override def getChar(offset: Int): Char = withParserVersion { delegateDoc.getChar(offset) }
  override def getContentType(offset: Int): String = withParserVersion { delegateDoc.getContentType(offset) }
  override def getDocumentPartitioner(): IDocumentPartitioner = withParserVersion { delegateDoc.getDocumentPartitioner }
  override def getLegalContentTypes(): Array[String] = withParserVersion { delegateDoc.getLegalContentTypes }
  override def getLegalLineDelimiters(): Array[String] = withParserVersion { delegateDoc.getLegalLineDelimiters }
  override def getLength(): Int = withParserVersion { delegateDoc.getLength }
  override def getLineDelimiter(line: Int): String = withParserVersion { delegateDoc.getLineDelimiter(line) }
  override def getLineInformation(line: Int): IRegion = withParserVersion { delegateDoc.getLineInformation(line) }
  override def getLineInformationOfOffset(offset: Int): IRegion = withParserVersion { delegateDoc.getLineInformationOfOffset(offset) }
  override def getLineLength(line: Int): Int = withParserVersion { delegateDoc.getLineLength(line) }
  override def getLineOffset(line: Int): Int = withParserVersion { delegateDoc.getLineOffset(line) }
  override def getNumberOfLines(offset: Int, length: Int): Int = withParserVersion { delegateDoc.getNumberOfLines(offset, length) }
  override def getNumberOfLines(): Int = withParserVersion { delegateDoc.getNumberOfLines }
  override def getPartition(offset: Int): ITypedRegion = withParserVersion { delegateDoc.getPartition(offset) }
  override def getPositionCategories(): Array[String] = withParserVersion { delegateDoc.getPositionCategories }
  override def getPositionUpdaters(): Array[IPositionUpdater] = withParserVersion { delegateDoc.getPositionUpdaters }
  override def getPositions(category: String): Array[Position] = withParserVersion { delegateDoc.getPositions(category) }
  override def insertPositionUpdater(updater: IPositionUpdater, index: Int): Unit = withParserVersion { delegateDoc.insertPositionUpdater(updater, index) }
  override def removeDocumentListener(listener: IDocumentListener): Unit = withParserVersion { delegateDoc.removeDocumentListener(listener) }
  override def removeDocumentPartitioningListener(listener: IDocumentPartitioningListener): Unit = withParserVersion { delegateDoc.removeDocumentPartitioningListener(listener) }
  override def removePosition(category: String, position: Position): Unit = withParserVersion { delegateDoc.removePosition(category, position) }
  override def removePosition(position: Position): Unit = withParserVersion { delegateDoc.removePosition(position) }
  override def removePositionCategory(category: String): Unit = withParserVersion { delegateDoc.removePositionCategory(category) }
  override def removePositionUpdater(updater: IPositionUpdater): Unit = withParserVersion { delegateDoc.removePositionUpdater(updater) }
  override def removePrenotifiedDocumentListener(documentAdapter: IDocumentListener): Unit = withParserVersion { delegateDoc.removePrenotifiedDocumentListener(documentAdapter) }
  override def replace(offset: Int, length: Int, text: String): Unit = withParserVersion { delegateDoc.replace(offset, length, text) }
  override def search(startOffset: Int, findString: String, forwardSearch: Boolean, caseSensitive: Boolean, wholeWord: Boolean): Int = withParserVersion { delegateDoc.search(startOffset, findString, forwardSearch, caseSensitive, wholeWord) }
  override def set(text: String): Unit = withParserVersion { delegateDoc.set(text) }
  override def setDocumentPartitioner(partitioner: IDocumentPartitioner): Unit = withParserVersion { delegateDoc.setDocumentPartitioner(partitioner) }

  // Members declared in org.eclipse.jface.text.IDocumentExtension
  override def registerPostNotificationReplace(listener: IDocumentListener, replace: IDocumentExtension.IReplace): Unit = withParserVersion { delegateDoc.registerPostNotificationReplace(listener, replace) }
  override def resumePostNotificationProcessing(): Unit = withParserVersion { delegateDoc.resumePostNotificationProcessing() }
  override def startSequentialRewrite(normalize: Boolean): Unit = withParserVersion { delegateDoc.startSequentialRewrite(normalize) }
  override def stopPostNotificationProcessing(): Unit = withParserVersion { delegateDoc.stopPostNotificationProcessing() }
  override def stopSequentialRewrite(): Unit = withParserVersion { delegateDoc.stopSequentialRewrite() }

  // Members declared in org.eclipse.wst.sse.core.internal.provisional.document.IEncodedDocument
  override def getPreferredLineDelimiter(): String = withParserVersion { delegateDoc.getPreferredLineDelimiter }
  override def setPreferredLineDelimiter(delim: String): Unit = withParserVersion { delegateDoc.setPreferredLineDelimiter(delim) }

  // Members declared in org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
  override def addDocumentAboutToChangeListener(listener: IModelAboutToBeChangedListener): Unit = withParserVersion { delegateDoc.addDocumentAboutToChangeListener(listener) }
  override def addDocumentChangedListener(listener: IStructuredDocumentListener): Unit = withParserVersion { delegateDoc.addDocumentChangedListener(listener) }
  override def addDocumentChangingListener(listener: IStructuredDocumentListener): Unit = withParserVersion { delegateDoc.addDocumentChangingListener(listener) }
  override def clearReadOnly(arg0: Int, arg1: Int): Unit = withParserVersion { delegateDoc.clearReadOnly(arg0, arg1) }
  override def containsReadOnly(arg0: Int, arg1: Int): Boolean = withParserVersion { delegateDoc.containsReadOnly(arg0, arg1) }
  override def getEncodingMemento(): EncodingMemento = withParserVersion { delegateDoc.getEncodingMemento }
  override def getFirstStructuredDocumentRegion(): IStructuredDocumentRegion = withParserVersion { delegateDoc.getFirstStructuredDocumentRegion }
  override def getLastStructuredDocumentRegion(): IStructuredDocumentRegion = withParserVersion { delegateDoc.getLastStructuredDocumentRegion }
  override def getLineDelimiter(): String = withParserVersion { delegateDoc.getLineDelimiter }
  override def getLineOfOffset(arg0: Int): Int = withParserVersion { delegateDoc.getLineOfOffset(arg0) }
  override def getParser(): RegionParser = withParserVersion { delegateDoc.getParser }
  override def getReParser(): IStructuredTextReParser = withParserVersion { delegateDoc.getReParser }
  override def getRegionAtCharacterOffset(arg0: Int): IStructuredDocumentRegion = withParserVersion { delegateDoc.getRegionAtCharacterOffset(arg0) }
  override def getRegionList(): IStructuredDocumentRegionList = withParserVersion { delegateDoc.getRegionList }
  override def getStructuredDocumentRegions(): Array[IStructuredDocumentRegion] = withParserVersion { delegateDoc.getStructuredDocumentRegions }
  override def getStructuredDocumentRegions(arg0: Int, arg1: Int): Array[IStructuredDocumentRegion] = withParserVersion { delegateDoc.getStructuredDocumentRegions(arg0, arg1) }
  override def getText(): String = withParserVersion { delegateDoc.getText }
  override def getUndoManager(): IStructuredTextUndoManager = withParserVersion { delegateDoc.getUndoManager }
  override def makeReadOnly(arg0: Int, arg1: Int): Unit = withParserVersion { delegateDoc.makeReadOnly(arg0, arg1) }
  override def newInstance(): IStructuredDocument = withParserVersion { delegateDoc.newInstance() }
  override def removeDocumentAboutToChangeListener(arg0: IModelAboutToBeChangedListener): Unit = withParserVersion { delegateDoc.removeDocumentAboutToChangeListener(arg0) }
  override def removeDocumentChangedListener(arg0: IStructuredDocumentListener): Unit = withParserVersion { delegateDoc.removeDocumentChangedListener(arg0) }
  override def removeDocumentChangingListener(arg0: IStructuredDocumentListener): Unit = withParserVersion { delegateDoc.removeDocumentChangingListener(arg0) }
  override def replaceText(arg0: Any, arg1: Int, arg2: Int, arg3: String, arg4: Boolean): StructuredDocumentEvent = withParserVersion { delegateDoc.replaceText(arg0, arg1, arg2, arg3, arg4) }
  override def replaceText(arg0: Any, arg1: Int, arg2: Int, arg3: String): StructuredDocumentEvent = withParserVersion { delegateDoc.replaceText(arg0, arg1, arg2, arg3) }
  override def setEncodingMemento(arg0: EncodingMemento): Unit = withParserVersion { delegateDoc.setEncodingMemento(arg0) }
  override def setLineDelimiter(arg0: String): Unit = withParserVersion { delegateDoc.setLineDelimiter(arg0) }
  override def setText(arg0: Any, arg1: String): StructuredDocumentEvent = withParserVersion { delegateDoc.setText(arg0, arg1) }
  override def setUndoManager(arg0: IStructuredTextUndoManager): Unit = withParserVersion { delegateDoc.setUndoManager(arg0) }
}
