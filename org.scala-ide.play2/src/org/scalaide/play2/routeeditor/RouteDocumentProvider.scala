package org.scalaide.play2.routeeditor

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.scalaide.play2.routeeditor.scanners.RoutePartitionScanner;

class RouteDocumentProvider extends FileDocumentProvider {
  protected override def createDocument(element: Object): IDocument = {
    val document = super.createDocument(element);
    if (document != null) {
      val partitioner = new FastPartitioner(
        new RoutePartitionScanner(),
        RoutePartitionScanner.getTypes())
      partitioner.connect(document)
      document.setDocumentPartitioner(partitioner)
    }
    document
  }
}