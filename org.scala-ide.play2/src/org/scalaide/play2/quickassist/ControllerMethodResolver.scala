package org.scalaide.play2.quickassist

import org.eclipse.jdt.core.ICompilationUnit
import scala.tools.eclipse.javaelements.ScalaCompilationUnit

/** Resolves a compilation unit and offset to an instance of `ControllerMethod`,
 *  if possible.
 *
 *  Different implementations for Java and Scala.
 *
 *  @note Implementers of this trait should be decoupled from the UI.
 */
trait ControllerMethodResolver {

  /** Return an instance of `ControllerMethod`, if the offset points to a Play controller method.
   *  A Play controller method is any method that returns an instance of `Action`
   *
   *  @param cu The compilation unit under inspection
   *  @param offset The offset in the compilation unit where the method definition is
   */
  def getControllerMethod(cu: ICompilationUnit, offset: Int): Option[ControllerMethod]
}

object ControllerMethodResovler {
  final val ActionClassFullName = "play.api.mvc.Action"
}

/** A Scala implementation for controller method resolution. */
class ScalaControllerMethodResolver extends ControllerMethodResolver {
  /** Extract a controller method from the Scala unit at the given offset.
   *
   *  A controller method is any method that returns an `Action`.
   */
  override def getControllerMethod(cu: ICompilationUnit, offset: Int): Option[ControllerMethod] = cu match {
    case scu: ScalaCompilationUnit =>
      scu.withSourceFile { (src, comp) =>
        import comp._

        /** Should only be called inside an `askOption`. */
        def actionClass = {
          comp.rootMirror.getClassIfDefined(ControllerMethodResovler.ActionClassFullName)
        }

        /* Is the symbol a method returning `play.api.mvc.Action`. */
        def isControllerMethod(sym: comp.Symbol) = (sym ne null) && (sym ne NoSymbol) && (comp.askOption { () =>
          lazy val returnsAction = sym.info.finalResultType.typeSymbol.isSubClass(actionClass)

          (sym.isMethod && sym.owner.isModuleOrModuleClass && returnsAction)
        } getOrElse false)

        val response = new Response[Tree]
        comp.askTypeAt(rangePos(src, offset, offset, offset), response)
        for {
          tree <- response.get.left.toOption
          sym = tree.symbol
          if isControllerMethod(sym)
        } yield ControllerMethod(sym.fullName, sym.paramss.flatten.map(param => (param.name.toString, param.tpe.toString)))

      }(None)

    case _ => None
  }
}