package org.scalaide.play2.routeeditor.hyperlink

import scala.tools.eclipse.InteractiveCompilationUnit
import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.hyperlink.text.Hyperlink
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.scalaide.play2.routeeditor.RouteAction
import scala.tools.eclipse.util.Utils

object RouteHyperlinkComputer {

  /** Return the hyperlink to controller referenced in this route, if it exists.
   */  
  def detectHyperlinks(scalaProject: ScalaProject, document: IDocument, region: IRegion, createJavaHyperlink: (RouteAction, IJavaElement) => IHyperlink): Option[IHyperlink] = {
    RouteAction.routeActionAt(document, region.getOffset()).flatMap {
      routeAction =>
        val routeActionParamTypes= routeAction.params.map(_._2)
        scalaProject.withPresentationCompiler { compiler =>
          import compiler._
          askOption { () =>

            // label for the hyperlink
            def methodLabel(method: Symbol): String = {
              method.fullNameString + method.paramss.map(_.map(_.tpe.toLongString).mkString("(", ",", ")")).mkString
            }

            // filter based on the method parameter types.
            def parametersMatch(method: Symbol): Boolean = {
              method.paramss match {
                case Nil =>
                  routeAction.params == Nil
                case firstSet :: Nil =>
                  val a = firstSet.map(_.tpe.toLongString)
                  a == routeActionParamTypes
                case _ =>
                  false // doesn't support multiple parameter list in the route file
              }
            }

            // object with the given name
            val obj = rootMirror.getModuleIfDefined(routeAction.typeName)

            // method or methods of the object with the given name
            val objMethod = obj.info.member(newTermName(routeAction.methodName))
            
            // if the method of the object doesn't exist, than perhaps routeAction.typeName is actually a class
            // so let's try getting the class and searching it's methods for routeAction.methodName
            val method =
              if (objMethod.exists) objMethod
              else 					rootMirror.getClassIfDefined(routeAction.typeName).info.member(newTermName(routeAction.methodName))

            if (!method.exists) {
              None
            } else {
              // find the method with the right parameter types 
              val filteredMethod = if (method.isOverloaded) {
                method.alternatives.find(parametersMatch(_))
              } else {
                Some(method).find(parametersMatch(_))
              }
              // generate the IHyperlink for the method
              val res = filteredMethod.flatMap {
                method =>
                  if (method.isJavaDefined) {
                    val elems =
                      (new MethodFinder(scalaProject.javaProject)).searchMethod(routeAction.fullName, routeAction.params.map(_._2).toArray)

                    elems.headOption.map {
                      createJavaHyperlink(routeAction, _)
                    }
                  } else {
                    locate(method, new FakeInteractiveCompilationUnitForProject(scalaProject)).map {
                      unitAndOffset =>
                        Hyperlink.withText("Open Declaration")(unitAndOffset._1, unitAndOffset._2, routeAction.methodName.length, methodLabel(method), routeAction.region)
                    }
                  }
              }
              res
            }
          }
        }().flatten
    }

  }

  /** Fake InteractiveCompilationUnit class, used to provide a Scala project to scala.tools.eclipse.LocateSymbol.locate().
   *  All methods except scalaProject throw UnsupportOperationException.
   *
   *  This won't be needed anymore after a fix for https://scala-ide-portfolio.assembla.com/spaces/scala-ide/tickets/1001672
   */
  private class FakeInteractiveCompilationUnitForProject(override val scalaProject: ScalaProject) extends InteractiveCompilationUnit {
    override def currentProblems(): List[org.eclipse.jdt.core.compiler.IProblem] = throw new UnsupportedOperationException
    override def exists(): Boolean = throw new UnsupportedOperationException
    override def file: tools.nsc.io.AbstractFile = throw new UnsupportedOperationException
    override def getContents(): Array[Char] = throw new UnsupportedOperationException
    override def reconcile(newContents: String): List[org.eclipse.jdt.core.compiler.IProblem] = throw new UnsupportedOperationException
    override def scheduleReconcile(): scala.tools.nsc.interactive.Response[Unit] = throw new UnsupportedOperationException
    override def sourceFile(contents: Array[Char]): tools.nsc.util.SourceFile = throw new UnsupportedOperationException
    override def workspaceFile: org.eclipse.core.resources.IFile = throw new UnsupportedOperationException
  }

}